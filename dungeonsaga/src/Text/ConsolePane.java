package Text;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Text.ConsolePane: um "console falso" dentro de uma janela Swing.
 *
 * <p>A ideia geral: o seu jogo continua usando System.out.println / System.out.print e
 * Scanner(System.in) normalmente, como se estivesse no console do IntelliJ. Esta classe
 * "sequestra" a entrada e a saída padrão do Java e as redireciona para um componente
 * visual (um painel preto com texto colorido e uma caixinha de digitação embaixo).</p>
 *
 * <p>Caminho que o texto percorre (SAÍDA do jogo para a tela):</p>
 * <ol>
 *   <li>O jogo chama System.out.print(...). Como trocamos o System.out por um PrintStream
 *       nosso (no construtor), os bytes vão para a classe AnsiOutputStream.</li>
 *   <li>AnsiOutputStream transforma os bytes em texto (UTF-8) e coloca o texto numa FILA.</li>
 *   <li>A thread da interface (EDT) esvazia a fila (drenarFila) e entrega o texto ao
 *       AnsiParser.</li>
 *   <li>O AnsiParser interpreta os códigos ANSI (cores e "limpar tela") e escreve o texto,
 *       já colorido, dentro do documento do JTextPane (a área de texto que você vê).</li>
 *   <li>Logo depois, trimDocument() apaga o texto que já saiu da tela (para não pesar) e
 *       scrollToBottom() rola a janela até o fim.</li>
 * </ol>
 *
 * <p>Caminho inverso (ENTRADA do teclado para o jogo): você digita na caixinha (inputField),
 * aperta Enter, o texto é escrito num "cano" (pipe) que foi ligado ao System.in. Quando o
 * jogo faz scanner.nextLine(), ele lê exatamente o que saiu desse cano.</p>
 *
 * <p>Sobre a "EDT": o Swing só é seguro se TODA mudança na interface acontecer numa única
 * thread especial, a Event Dispatch Thread. O jogo roda em outra thread, então sempre que
 * ele precisa mexer na tela usamos SwingUtilities.invokeLater(...), que significa "execute
 * isto na thread da interface, assim que possível".</p>
 */
public class ConsolePane extends JPanel {

    /**
     * A área de texto onde tudo aparece. É um JTextPane (texto com estilos: cores, negrito...),
     * diferente de um JTextArea, que só guarda texto puro.
     *
     * <p>Sobrescrevemos dois métodos de "rolagem": getScrollableTracksViewportWidth = true
     * faz o texto QUEBRAR LINHA na largura da janela (em vez de criar rolagem horizontal);
     * getScrollableTracksViewportHeight = false deixa o texto crescer para baixo, e a
     * rolagem vertical cuida do resto.</p>
     */
    private final JTextPane textPane = new JTextPane() {
        @Override public boolean getScrollableTracksViewportWidth()  { return true;  }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    };

    /** A caixinha onde você digita as respostas do jogo (fica embaixo da tela). */
    private final JTextField inputField = new JTextField();

    /** A "moldura com barra de rolagem" que envolve o textPane. */
    private final JScrollPane scrollPane;

    /**
     * Estilo ATUAL do texto (cor da letra, cor do fundo, negrito). Quando o parser lê um
     * código de cor como "ESC[32m", ele muda este objeto; todo texto escrito depois usa
     * esse estilo, até aparecer outro código de cor. É o mesmo comportamento de um terminal.
     */
    private final MutableAttributeSet attrs = new SimpleAttributeSet();

    /** Ponta de ESCRITA do cano que alimenta o System.in (o que você digita entra por aqui). */
    private final PipedOutputStream pipedOut;

    /** O "tradutor" de códigos ANSI. Guarda estado entre uma chamada e outra (veja a classe). */
    private final AnsiParser parser = new AnsiParser();

    /** Fonte já escolhida, guardada para não repetir a busca (e os avisos) a cada uso. */
    private static Font FONTE_CACHE;

    // ------------------------------------------------------------------------------------
    // Fila de saída e controles de desempenho
    // ------------------------------------------------------------------------------------

    /**
     * Fila de texto que o jogo já imprimiu mas que a tela ainda não processou.
     * É escrita pela thread do jogo e lida pela thread da interface, por isso todo acesso a
     * ela é feito dentro de synchronized (filaSaida) { ... }.
     */
    private final StringBuilder filaSaida = new StringBuilder();

    /**
     * true = já existe um pedido pendente para a EDT esvaziar a fila. Evita agendar
     * milhares de tarefas quando o jogo imprime muitos pedacinhos seguidos: a EDT esvazia
     * tudo de uma vez só. Protegida pelo mesmo synchronized (filaSaida).
     */
    private boolean drenagemAgendada = false;

    /** true = já existe um pedido pendente de "rolar até o fim" (evita pedidos repetidos). */
    private boolean scrollAgendado = false;

    /** Momento (System.nanoTime) da última vez que medimos a altura para apagar o excesso. */
    private long ultimaVerificacaoNs = System.nanoTime() - 1_000_000_000L;

    /**
     * QUANTAS LINHAS VISUAIS guardar além das que cabem na janela. É o "pouco depois de sair
     * do frame": o texto fica mais ~15 linhas acima da borda de cima, e depois é descartado.
     * Aumente se quiser poder rolar mais para cima; diminua para apagar mais cedo.
     */
    private static final int LINHAS_EXTRAS_MANTIDAS = 15;

    /**
     * Folga (em linhas) antes de apagar. Se apagássemos a cada linha nova, o programa gastaria
     * tempo apagando o tempo todo. Em vez disso deixamos o excesso crescer até esta folga e
     * apagamos de uma vez. Resultado: sobram entre LINHAS_EXTRAS_MANTIDAS e
     * LINHAS_EXTRAS_MANTIDAS + esta folga linhas acima da janela.
     */
    private static final int LINHAS_FOLGA_ANTES_DE_APAGAR = 15;

    /** Intervalo mínimo entre duas medições de altura (medir força o Swing a calcular o layout). */
    private static final long INTERVALO_VERIFICACAO_MS = 100;

    /**
     * Rede de segurança por NÚMERO DE CARACTERES: se por algum motivo a medição por altura
     * não funcionar (ex.: janela ainda não apareceu), o documento nunca passa de
     * LIMITE_MAX_CHARS; ao passar, é cortado até sobrar LIMITE_MIN_CHARS.
     */
    private static final int LIMITE_MAX_CHARS = 60_000;
    private static final int LIMITE_MIN_CHARS = 40_000;

    /** Quantos caracteres procurar à frente de um corte até achar uma quebra de linha limpa. */
    private static final int MARGEM = 500;

    // ------------------------------------------------------------------------------------
    // CONSTRUTOR
    // ------------------------------------------------------------------------------------

    /**
     * Monta a janela inteira e liga o console falso ao resto do programa.
     *
     * <p>Em ordem: (1) configura e estiliza o painel de texto; (2) cria a barra de rolagem;
     * (3) configura a caixinha de digitação; (4) monta o layout (texto no centro, caixinha
     * embaixo); (5) cria o "cano" e o liga ao System.in; (6) define o que acontece quando
     * você aperta Enter; (7) troca System.out e System.err pelo nosso PrintStream.</p>
     *
     * <p>Atenção: depois do passo 7, tudo que for impresso vai para o painel, INCLUSIVE
     * mensagens de erro. Mensagens impressas ANTES (como os avisos de carregarFonte, que
     * rodam durante o construtor) aparecem no console do IntelliJ.</p>
     */
    public ConsolePane() {
        super(new BorderLayout());

        // --- painel de texto: preto, não editável, com a fonte escolhida ---
        textPane.setEditable(false);
        textPane.setBackground(Color.BLACK);
        textPane.setForeground(Color.WHITE);
        textPane.setFont(carregarFonte());
        textPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // --- barra de rolagem: vertical sempre visível, horizontal nunca ---
        scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // --- FUNDO PRETO EM TODAS AS CAMADAS ---
        // O JScrollPane tem camadas: o painel (this), o scrollPane, o "viewport" (a janelinha
        // por onde se enxerga o texto) e, por cima, o textPane. O textPane só cobre a parte do
        // viewport onde existe texto. Quando o texto é curto (por exemplo logo depois do
        // "limpa tela" do jogo) sobra área vazia, e ali aparece o fundo do VIEWPORT, que por
        // padrão é cinza claro/branco do tema do Swing. Pintando todas as camadas de preto,
        // essa área vazia fica preta, em qualquer tamanho de janela.
        setBackground(Color.BLACK);
        setOpaque(true);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.getViewport().setOpaque(true);

        // --- barra de rolagem vertical escura (veja a classe BarraDeRolagemEscura) ---
        JScrollBar barra = scrollPane.getVerticalScrollBar();
        barra.setUI(new BarraDeRolagemEscura());
        barra.setBackground(Color.BLACK);
        barra.setPreferredSize(new Dimension(12, 0));   // 12 pixels de largura (a altura é automática)

        // --- caixinha de digitação ---
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(carregarFonte());
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        // --- layout: texto ocupa o centro, caixinha fica embaixo ---
        add(scrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        // --- estilo inicial do texto: letra branca em fundo preto ---
        StyleConstants.setForeground(attrs, Color.WHITE);
        StyleConstants.setBackground(attrs, Color.BLACK);

        // --- cano da entrada: tudo que for escrito em pipedOut sai em System.in ---
        try {
            pipedOut = new PipedOutputStream();
            System.setIn(new PipedInputStream(pipedOut, 8192));
        } catch (IOException e) { throw new RuntimeException(e); }

        // --- quando você aperta Enter na caixinha ---
        inputField.addActionListener(e -> {
            String linha = inputField.getText();
            inputField.setText("");
            appendEco(linha + "\n");   // mostra na tela o que você digitou (o "eco")
            try {
                // manda a linha para o jogo (ele a lê com Scanner/BufferedReader)
                pipedOut.write((linha + "\n").getBytes(StandardCharsets.UTF_8));
                pipedOut.flush();
            } catch (IOException ignored) {}
            inputField.requestFocusInWindow();
        });

        // --- sequestra a saída: System.out e System.err passam a escrever no painel ---
        // autoFlush = true: cada print/println é entregue na hora (sem ficar guardado).
        PrintStream ps = new PrintStream(new AnsiOutputStream(), true, StandardCharsets.UTF_8);
        System.setOut(ps);
        System.setErr(ps);

        // quando a janela abrir, o cursor já começa na caixinha de digitação
        SwingUtilities.invokeLater(inputField::requestFocusInWindow);
    }

    /**
     * Chamado pelo Swing quando este painel é colocado dentro de uma janela de verdade (ou
     * seja, depois do frame.add(new Text.ConsolePane()) ou frame.setContentPane(...)).
     *
     * <p>Aproveitamos para pintar de preto a JANELA e o painel de conteúdo dela. Isso evita
     * um clarão branco quando você redimensiona a janela: durante o arrasto, o Swing mostra
     * por instantes o fundo da própria janela, antes de redesenhar o nosso painel por cima.</p>
     */
    @Override
    public void addNotify() {
        super.addNotify();
        Window janela = SwingUtilities.getWindowAncestor(this);
        if (janela != null) {
            janela.setBackground(Color.BLACK);
            if (janela instanceof RootPaneContainer) {
                ((RootPaneContainer) janela).getContentPane().setBackground(Color.BLACK);
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // ESCRITA NA TELA
    // ------------------------------------------------------------------------------------

    /**
     * Escreve na tela o que VOCÊ digitou (o "eco"), igual a um terminal real, que mostra as
     * teclas que você aperta.
     *
     * <p>Roda dentro de invokeLater porque mexe na interface (precisa estar na EDT).
     * Insere o texto no FIM do documento e avisa o parser (cursorPos) que o cursor de escrita
     * agora está no fim, para a próxima saída do jogo vir depois do eco. Depois apaga o
     * excesso e rola para baixo.</p>
     */
    private void appendEco(String s) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = textPane.getStyledDocument();
                doc.insertString(doc.getLength(), s, attrs);
                parser.cursorPos = doc.getLength();
                trimDocument();
                scrollToBottom();
            } catch (BadLocationException ignored) {}
        });
    }

    /**
     * Rola a janela até o final, para você sempre ver o texto mais novo.
     *
     * <p>Duas ações: move o cursor de texto (caret) para o fim do documento e empurra a barra
     * de rolagem até o máximo. Isso é agendado com invokeLater para acontecer DEPOIS de o
     * Swing recalcular o tamanho do texto (senão a barra "máximo" ainda seria o antigo).</p>
     *
     * <p>Melhoria de desempenho: se já existe um pedido de rolagem esperando, não criamos
     * outro (scrollAgendado). Assim, mesmo que cheguem 1000 pedaços de texto seguidos,
     * rolamos só uma vez.</p>
     */
    private void scrollToBottom() {
        if (scrollAgendado) return;
        scrollAgendado = true;
        SwingUtilities.invokeLater(() -> {
            scrollAgendado = false;
            try { textPane.setCaretPosition(textPane.getDocument().getLength()); }
            catch (Exception ignored) {}
            JScrollBar vbar = scrollPane.getVerticalScrollBar();
            vbar.setValue(vbar.getMaximum());
        });
    }

    /**
     * Esvazia a fila de saída e desenha tudo de uma vez. Sempre roda na EDT.
     *
     * <p>Passos: (1) pega TODO o texto acumulado na fila e a zera; (2) entrega o texto ao
     * parser, que o escreve com as cores certas; (3) apaga o que já saiu da tela; (4) rola
     * até o fim.</p>
     *
     * <p>Por que isso ajuda no lag: antes, cada print do jogo virava uma tarefa separada e
     * o Swing recalculava o layout do texto a cada uma. Agora, se o jogo imprime muito
     * rápido (como no tsunami), as tarefas se juntam e o recálculo acontece uma vez por lote.</p>
     */
    private void drenarFila() {
        String texto;
        synchronized (filaSaida) {
            texto = filaSaida.toString();
            filaSaida.setLength(0);
            drenagemAgendada = false;   // a partir daqui, um novo flush pode agendar outra drenagem
        }
        if (texto.isEmpty()) return;
        parser.feed(texto, textPane, attrs);
        trimDocument();
        scrollToBottom();
    }

    // ------------------------------------------------------------------------------------
    // DESCARTE DO TEXTO ANTIGO (evita acumular "para sempre" e o lag)
    // ------------------------------------------------------------------------------------

    /**
     * Apaga do INÍCIO do documento o texto que já saiu da janela há um tempinho.
     *
     * <p>Duas estratégias, nesta ordem:</p>
     * <ol>
     *   <li>Por ALTURA (principal): mede a altura total do texto e apaga o que sobra acima de
     *       "altura da janela + LINHAS_EXTRAS_MANTIDAS linhas". É por altura (linhas visuais),
     *       e não por quebras de linha, porque o tsunami é uma única linha lógica enorme
     *       (sem "\n") que o Swing quebra em muitas linhas visuais.</li>
     *   <li>Por CARACTERES (rede de segurança): se o texto passar de LIMITE_MAX_CHARS
     *       mesmo assim, corta até LIMITE_MIN_CHARS.</li>
     * </ol>
     *
     * <p>A medição de altura é limitada a uma vez a cada INTERVALO_VERIFICACAO_MS, pois
     * medir obriga o Swing a calcular o layout.</p>
     */
    private void trimDocument() {
        StyledDocument doc = textPane.getStyledDocument();
        int apagarAte = 0;

        long agora = System.nanoTime();
        if (agora - ultimaVerificacaoNs >= INTERVALO_VERIFICACAO_MS * 1_000_000L) {
            ultimaVerificacaoNs = agora;
            apagarAte = calcularCortePorAltura(doc);
        }
        if (apagarAte <= 0 && doc.getLength() > LIMITE_MAX_CHARS) {
            apagarAte = calcularCortePorCaracteres(doc);
        }
        if (apagarAte > 0) {
            apagarInicio(doc, apagarAte);
        }
    }

    /**
     * Descobre ATÉ QUE POSIÇÃO do texto podemos apagar, olhando a altura em pixels.
     * Devolve 0 se ainda não há nada para apagar.
     *
     * <p>Raciocínio com números: altura total = onde termina o último texto (em pixels,
     * contando do topo do documento). Queremos manter "manter" pixels (janela + linhas
     * extras). Se a altura total só passou "manter" por menos que a folga, não fazemos nada.
     * Se passou, o corte fica no pixel (altura total - manter): tudo ACIMA dele é descartado.</p>
     *
     * <p>Para converter esse pixel numa posição do texto usamos uma BUSCA BINÁRIA (bisseção):
     * olhamos o caractere do meio, vemos em que altura ele está, e descartamos a metade que
     * não interessa; repetindo, achamos em cerca de 17 passos o primeiro caractere que está
     * abaixo do corte (o começo de uma linha visual, então as linhas que sobram continuam
     * quebradas exatamente igual).</p>
     *
     * <p>Por que não usar o método pronto viewToModel2D(ponto)? Porque ele depende do
     * tamanho ATUAL do componente na tela, que o Swing só atualiza um pouco depois de o texto
     * crescer. Com o tamanho desatualizado ele responde "fim do documento" e apagaria TUDO
     * (testei e aconteceu). Já modelToView2D(posição) usa o layout do texto, que está sempre
     * correto, então a busca binária é segura.</p>
     */
    private int calcularCortePorAltura(StyledDocument doc) {
        int alturaJanela = scrollPane.getViewport().getHeight();
        int len = doc.getLength();
        if (alturaJanela <= 0 || len == 0) return 0;   // janela ainda não apareceu
        try {
            int alturaLinha = textPane.getFontMetrics(textPane.getFont()).getHeight();
            Rectangle2D fim = textPane.modelToView2D(len);   // posição do último caractere
            if (fim == null) return 0;

            double alturaTotal = fim.getMaxY();
            double manter = alturaJanela + (double) LINHAS_EXTRAS_MANTIDAS * alturaLinha;
            double folga  = (double) LINHAS_FOLGA_ANTES_DE_APAGAR * alturaLinha;
            if (alturaTotal <= manter + folga) return 0;

            double yCorte = alturaTotal - manter;

            // busca binária: primeiro caractere cuja altura (y) é >= yCorte
            int lo = 0, hi = len;
            while (lo < hi) {
                int meio = (lo + hi) >>> 1;
                Rectangle2D r = textPane.modelToView2D(meio);
                if (r == null) return 0;
                if (r.getY() >= yCorte) hi = meio; else lo = meio + 1;
            }
            // proteção: nunca apaga tudo (sempre sobra o final do texto)
            return (lo >= len) ? 0 : lo;
        } catch (BadLocationException e) {
            return 0;
        }
    }

    /**
     * Rede de segurança: se o documento passou de LIMITE_MAX_CHARS, devolve a posição até
     * onde apagar para sobrar cerca de LIMITE_MIN_CHARS. Para não cortar uma linha no meio,
     * avança até a próxima quebra de linha (procurando no máximo MARGEM caracteres).
     * Devolve 0 se não precisa apagar.
     */
    private int calcularCortePorCaracteres(StyledDocument doc) {
        int len = doc.getLength();
        if (len <= LIMITE_MAX_CHARS) return 0;
        int remover = len - LIMITE_MIN_CHARS;
        try {
            int scan = Math.min(MARGEM, len - remover);
            int nl = doc.getText(remover, scan).indexOf('\n');
            if (nl >= 0) remover += nl + 1;
        } catch (BadLocationException ignored) {}
        return remover;
    }

    /**
     * Apaga de fato os primeiros "ate" caracteres do documento.
     *
     * <p>Detalhe importante: o parser guarda cursorPos, a posição onde o próximo texto será
     * escrito. Como tudo deslocou "ate" posições para trás, subtraímos o mesmo valor (sem
     * deixar ficar negativo), senão o cursor ficaria apontando para o lugar errado.</p>
     */
    private void apagarInicio(StyledDocument doc, int ate) {
        try {
            doc.remove(0, ate);
        } catch (BadLocationException ignored) {
            return;
        }
        parser.cursorPos = Math.max(0, parser.cursorPos - ate);
    }

    // ------------------------------------------------------------------------------------
    // BARRA DE ROLAGEM
    // ------------------------------------------------------------------------------------

    /**
     * Visual escuro para a barra de rolagem, combinando com o fundo preto do console.
     *
     * <p>Como funciona: o Swing separa "o que a barra faz" (o JScrollBar) de "como ela é
     * desenhada" (o objeto de UI). Aqui trocamos só o desenho, herdando de BasicScrollBarUI
     * (o desenho básico, igual em qualquer tema) e sobrescrevendo quatro coisas:</p>
     * <ul>
     *   <li>paintTrack: pinta a "pista" (o trilho por onde o botão desliza) de quase preto;</li>
     *   <li>paintThumb: pinta o "polegar" (o pedaço que você arrasta) como um retângulo
     *       arredondado cinza escuro, que clareia quando o mouse passa por cima ou você arrasta;</li>
     *   <li>createDecreaseButton / createIncreaseButton: trocam as setinhas de cima e de baixo
     *       por botões de tamanho zero (somem). Você continua rolando com a roda do mouse,
     *       arrastando o polegar ou clicando na pista.</li>
     * </ul>
     * A rolagem em si (inclusive a rolagem automática para o fim) não muda em nada.
     */
    private static class BarraDeRolagemEscura extends BasicScrollBarUI {

        /** Cor da pista (quase preto, um tiquinho mais claro que o fundo do texto). */
        private static final Color COR_PISTA = new Color(18, 18, 18);
        /** Cor normal do polegar. */
        private static final Color COR_POLEGAR = new Color(80, 80, 80);
        /** Cor do polegar com o mouse em cima ou sendo arrastado. */
        private static final Color COR_POLEGAR_ATIVO = new Color(125, 125, 125);

        /** Pinta a pista (o fundo da barra). "area" é o retângulo que a pista ocupa. */
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle area) {
            g.setColor(COR_PISTA);
            g.fillRect(area.x, area.y, area.width, area.height);
        }

        /**
         * Pinta o polegar. Usa antialiasing (bordas suaves) e deixa 2 pixels de margem de
         * cada lado para ele não encostar nas bordas da barra. Se o polegar não cabe
         * (area vazia), não pinta nada.
         */
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle area) {
            if (area.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor((isDragging || isThumbRollover()) ? COR_POLEGAR_ATIVO : COR_POLEGAR);
            g2.fillRoundRect(area.x + 2, area.y + 1, area.width - 4, area.height - 2, 8, 8);
            g2.dispose();
        }

        /** Botão de cima: um botão de tamanho zero (some, em vez de mostrar a setinha). */
        @Override
        protected JButton createDecreaseButton(int orientation) { return botaoInvisivel(); }

        /** Botão de baixo: um botão de tamanho zero (some, em vez de mostrar a setinha). */
        @Override
        protected JButton createIncreaseButton(int orientation) { return botaoInvisivel(); }

        /**
         * O BasicScrollBarUI exige que existam os dois botões (não aceita null), então
         * criamos botões com largura e altura zero: existem, mas não aparecem.
         */
        private static JButton botaoInvisivel() {
            JButton b = new JButton();
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            b.setFocusable(false);
            return b;
        }
    }

    // ------------------------------------------------------------------------------------
    // SAÍDA: bytes do System.out -> fila -> tela
    // ------------------------------------------------------------------------------------

    /**
     * O "cano" no qual o nosso PrintStream escreve. Cada print do jogo acaba chegando aqui
     * como bytes. Junta os bytes num buffer e, no flush(), converte o buffer em texto
     * (UTF-8, por isso o braille e os blocos funcionam) e o coloca na fila para a EDT desenhar.
     *
     * <p>write(int) é chamado byte a byte pelo PrintStream; flush() é chamado a cada print
     * (porque criamos o PrintStream com autoFlush = true).</p>
     */
    private class AnsiOutputStream extends OutputStream {
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

        /** Guarda um byte no buffer. */
        @Override public void write(int b) { buf.write(b); }

        /**
         * Converte tudo que está no buffer em texto e o coloca na fila de saída. Só agenda
         * uma drenagem se ainda não houver uma pendente (as chamadas seguintes apenas
         * acrescentam texto à fila e a drenagem já agendada o levará junto).
         */
        @Override public void flush() {
            if (buf.size() == 0) return;
            String s = buf.toString(StandardCharsets.UTF_8);
            buf.reset();

            boolean precisaAgendar;
            synchronized (filaSaida) {
                filaSaida.append(s);
                precisaAgendar = !drenagemAgendada;
                drenagemAgendada = true;
            }
            if (precisaAgendar) SwingUtilities.invokeLater(ConsolePane.this::drenarFila);
        }
    }

    // ------------------------------------------------------------------------------------
    // PARSER ANSI
    // ------------------------------------------------------------------------------------

    /**
     * Interpretador de "códigos ANSI": o jeito padrão de os terminais colorirem texto.
     *
     * <p>Um código ANSI é uma sequência escondida no meio do texto: o caractere ESC (código
     * 27), depois '[', depois números separados por ';' e uma letra final. Por exemplo, ESC [ 32 m
     * significa "a partir de agora, letra verde"; ESC [ 0 m significa "volte ao normal".
     * Letras finais tratadas aqui:</p>
     * <ul>
     *   <li>'m' = cores e negrito (método applySgr);</li>
     *   <li>'J' = apagar a tela (método apagarTela). O jogo usa ESC[2J ("limpa tela");</li>
     *   <li>'H' ou 'f' = mover o cursor para o canto superior esquerdo (método cursorParaInicio).
     *       O jogo usa ESC[H logo antes de ESC[2J;</li>
     *   <li>qualquer outra (ex.: ESC[?25l, que esconde o cursor) é reconhecida e IGNORADA.</li>
     * </ul>
     *
     * <p>Os códigos são engolidos: o texto "ESC[2J" nunca aparece na tela.</p>
     *
     * <p>Limitação: um terminal de verdade tem uma "tela" com linhas e colunas fixas. Este painel
     * é só uma lista de texto que cresce (um "log" com rolagem), então não existe posição
     * (linha, coluna) arbitrária. Por isso só entendemos o cursor "home" e o apagar-tela.</p>
     *
     * <p>Esta classe guarda ESTADO entre uma chamada e outra porque um código pode chegar
     * cortado ao meio (metade num pedaço de texto, metade no seguinte). O que sobra de
     * incompleto fica em "pending" esperando o próximo pedaço.</p>
     */
    private static class AnsiParser {

        /** Texto recebido que ainda não foi totalmente processado (ex.: código cortado). */
        private final StringBuilder pending = new StringBuilder();

        /**
         * Posição do documento onde o próximo texto será escrito (o "cursor de escrita" de um
         * terminal). Normalmente é o fim do documento; o caractere '\r' o leva ao começo da
         * linha atual, permitindo "sobrescrever" a linha (usado em animações).
         */
        int cursorPos = 0;

        /**
         * Processa um pedaço de texto: separa o que é texto comum do que é código ANSI,
         * escreve o texto no documento e aplica os códigos de cor ao estilo atual (attrs).
         *
         * <p>Duas posições andam pelo texto: "i" é onde estamos lendo agora; "processed" é até
         * onde já escrevemos no documento. Quando achamos um ESC, primeiro escrevemos o texto
         * comum acumulado entre "processed" e "i", e só depois tratamos o código.</p>
         */
        void feed(String text, JTextPane pane, MutableAttributeSet attrs) {
            pending.append(text);
            StyledDocument doc = pane.getStyledDocument();

            int processed = 0;
            int i = 0;
            while (i < pending.length()) {
                char c = pending.charAt(i);
                if (c == '\u001B') {                                    // achou o caractere ESC
                    if (i + 1 >= pending.length()) break;               // ESC no fim: espera o resto
                    if (pending.charAt(i + 1) != '[') {                 // ESC solto (anomalia): descarta
                        if (processed < i) writeText(doc, pending.substring(processed, i), attrs);
                        i++; processed = i;
                        continue;
                    }
                    // procura a letra que termina o código (ex.: o 'm' de "ESC[32m")
                    int j = i + 2;
                    while (j < pending.length() && !Character.isLetter(pending.charAt(j))) j++;
                    if (j >= pending.length()) break;                   // código incompleto: espera mais
                    if (processed < i) writeText(doc, pending.substring(processed, i), attrs);
                    String params = pending.substring(i + 2, j);   // os números entre '[' e a letra final
                    char letraFinal = pending.charAt(j);
                    if (letraFinal == 'm')                           applySgr(params, attrs);   // cores / negrito
                    else if (letraFinal == 'J')                      apagarTela(doc, params);   // limpar tela
                    else if (letraFinal == 'H' || letraFinal == 'f') cursorParaInicio(params);  // cursor "home"
                    // qualquer outra letra (ex.: 'l' de ESC[?25l, que esconde o cursor) é ignorada
                    i = j + 1;
                    processed = i;
                } else {
                    i++;
                }
            }
            int fim = i;
            // Um '\r' no fim do pedaço pode ser a primeira metade de um "\r\n" que veio
            // partido: segura ele até o próximo pedaço (o efeito do '\r' só importa no que vem depois).
            if (fim > processed && pending.charAt(fim - 1) == '\r') fim--;
            if (processed < fim) {
                writeText(doc, pending.substring(processed, fim), attrs);
                processed = fim;
            }
            if (processed > 0) pending.delete(0, processed);
        }

        /**
         * Escreve texto comum (sem códigos ANSI) no documento, respeitando três caracteres
         * especiais:
         * <ul>
         *   <li>'\r' sozinho: volta o cursor de escrita ao começo da linha (o próximo texto
         *       sobrescreve a linha). Se vier como "\r\n" (o println do Windows), é só uma
         *       quebra de linha comum e o '\r' é ignorado. Foi isso que fazia o braille sumir
         *       antes: cada linha sobrescrevia a anterior.</li>
         *   <li>'\n': insere uma quebra de linha na posição do cursor de escrita.</li>
         *   <li>qualquer outro texto: se o cursor está no fim, só acrescenta; se está no meio
         *       (depois de um '\r'), SOBRESCREVE os caracteres que já estavam ali, sem passar
         *       do fim da linha.</li>
         * </ul>
         */
        private void writeText(StyledDocument doc, String s, AttributeSet a) {
            try {
                int i = 0;
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c == '\r') {
                        // "\r\n" (println no Windows) é só uma quebra de linha: ignora o '\r'.
                        // Sem isso, cada linha impressa sobrescrevia a anterior.
                        if (i + 1 < s.length() && s.charAt(i + 1) == '\n') { i++; continue; }
                        cursorPos = findLineStart(doc, cursorPos);
                        i++;
                    } else if (c == '\n') {
                        int at = Math.min(cursorPos, doc.getLength());
                        doc.insertString(at, "\n", a);
                        cursorPos = at + 1;
                        i++;
                    } else {
                        // pega o trecho de texto comum até o próximo '\r' ou '\n'
                        int end = i;
                        while (end < s.length() && s.charAt(end) != '\r' && s.charAt(end) != '\n') end++;
                        String run = s.substring(i, end);
                        if (cursorPos >= doc.getLength()) {
                            // caso comum: cursor no fim, só acrescenta
                            doc.insertString(doc.getLength(), run, a);
                            cursorPos = doc.getLength();
                        } else {
                            // cursor no meio da linha: sobrescreve o que já existia
                            int searchLen = Math.min(run.length(), doc.getLength() - cursorPos);
                            String existing = doc.getText(cursorPos, searchLen);
                            int nlIdx = existing.indexOf('\n');
                            int ow = (nlIdx < 0) ? existing.length() : nlIdx;
                            if (ow > 0) doc.remove(cursorPos, ow);
                            doc.insertString(cursorPos, run, a);
                            cursorPos += run.length();
                        }
                        i = end;
                    }
                }
            } catch (BadLocationException ignored) {}
        }

        /**
         * Devolve a posição do começo da linha que contém "pos". Usa os "parágrafos" do
         * documento (cada quebra de linha cria um): o começo do parágrafo é o começo da
         * linha. É bem mais rápido do que copiar o texto todo para procurar o último '\n'.
         */
        private int findLineStart(StyledDocument doc, int pos) {
            if (pos <= 0) return 0;
            return doc.getParagraphElement(Math.min(pos, doc.getLength())).getStartOffset();
        }

        /**
         * Trata o código ESC[nJ ("Erase in Display", apagar a tela). O número n diz o quê apagar:
         * <ul>
         *   <li>2 (ou 3): apaga TUDO e o cursor de escrita volta ao começo. É o ESC[2J que o
         *       Demo.Demo manda a cada volta do laço para "limpar a tela";</li>
         *   <li>0 (ou vazio): apaga do cursor de escrita até o fim do texto;</li>
         *   <li>1: apaga do começo do texto até o cursor de escrita.</li>
         * </ul>
         *
         * <p>As cores atuais (attrs) NÃO mudam ao limpar: um terminal real também mantém a cor
         * ativa depois de limpar a tela.</p>
         *
         * <p>Bônus de desempenho: limpar a tela joga fora o texto antigo na hora, então o
         * documento volta a ficar minúsculo no início de cada volta do jogo.</p>
         *
         * <p>Para DESLIGAR este comportamento (voltar a só acumular texto), comente a linha
         * "else if (letraFinal == 'J') ..." dentro do método feed.</p>
         */
        private void apagarTela(StyledDocument doc, String params) {
            int n = 0;
            if (params != null && !params.isEmpty()) {
                try { n = Integer.parseInt(params); } catch (NumberFormatException e) { return; }
            }
            int len = doc.getLength();
            int pos = Math.max(0, Math.min(cursorPos, len));
            try {
                if (n == 2 || n == 3) {
                    doc.remove(0, len);
                    cursorPos = 0;
                } else if (n == 0) {
                    doc.remove(pos, len - pos);
                    cursorPos = pos;
                } else if (n == 1) {
                    doc.remove(0, pos);
                    cursorPos = 0;
                }
            } catch (BadLocationException ignored) {}
        }

        /**
         * Trata o código ESC[H ou ESC[1;1H ("Cursor Position", cursor para o canto superior
         * esquerdo): o cursor de escrita volta para o COMEÇO do texto, e o próximo texto
         * sobrescreve o que estiver ali. Se o código trouxer outra linha/coluna (ex.: ESC[5;10H),
         * ele é ignorado, porque aqui não existe uma grade de linhas e colunas (veja a
         * limitação descrita na classe).
         *
         * <p>Combinado com ESC[2J (que apaga tudo logo em seguida), o efeito é exatamente o
         * "limpar a tela e recomeçar do topo" que o jogo quer.</p>
         */
        private void cursorParaInicio(String params) {
            boolean home = params == null || params.isEmpty() || params.equals(";")
                    || params.equals("1;1") || params.equals("1");
            if (home) cursorPos = 0;
        }

        /**
         * Aplica um código de cor ("SGR") ao estilo atual. Recebe só os números do meio
         * (ex.: "1;32" vindo de ESC[1;32m) e os trata um a um:
         * <ul>
         *   <li>0 = reset (letra branca, fundo preto, sem negrito); 1 = negrito;</li>
         *   <li>30 a 37 / 90 a 97 = cor da letra (normal / brilhante);</li>
         *   <li>40 a 47 / 100 a 107 = cor do fundo (normal / brilhante);</li>
         *   <li>38;5;N e 48;5;N = cor da letra / fundo na paleta de 256 cores;</li>
         *   <li>38;2;R;G;B e 48;2;R;G;B = cor exata (RGB), a chamada "truecolor".</li>
         * </ul>
         * Códigos que não conhecemos (sublinhado, itálico, etc.) são simplesmente ignorados.
         */
        private void applySgr(String codes, MutableAttributeSet attrs) {
            if (codes == null || codes.isEmpty()) codes = "0";
            String[] parts = codes.split(";");
            for (int i = 0; i < parts.length; i++) {
                int v;
                try { v = Integer.parseInt(parts[i]); } catch (NumberFormatException e) { continue; }
                if (v == 0) {
                    StyleConstants.setForeground(attrs, Color.WHITE);
                    StyleConstants.setBackground(attrs, Color.BLACK);
                    StyleConstants.setBold(attrs, false);
                } else if (v == 1) StyleConstants.setBold(attrs, true);
                else if (v >= 30 && v <= 37) StyleConstants.setForeground(attrs, ansiColor(v - 30, false));
                else if (v >= 90 && v <= 97) StyleConstants.setForeground(attrs, ansiColor(v - 90, true));
                else if (v >= 40 && v <= 47) StyleConstants.setBackground(attrs, ansiColor(v - 40, false));
                else if (v >= 100 && v <= 107) StyleConstants.setBackground(attrs, ansiColor(v - 100, true));
                else if (v == 38 && i + 2 < parts.length && "5".equals(parts[i+1])) {
                    StyleConstants.setForeground(attrs, color256(Integer.parseInt(parts[i+2]))); i += 2;
                } else if (v == 48 && i + 2 < parts.length && "5".equals(parts[i+1])) {
                    StyleConstants.setBackground(attrs, color256(Integer.parseInt(parts[i+2]))); i += 2;
                } else if (v == 38 && i + 4 < parts.length && "2".equals(parts[i+1])) {
                    StyleConstants.setForeground(attrs, new Color(
                            Integer.parseInt(parts[i+2]), Integer.parseInt(parts[i+3]), Integer.parseInt(parts[i+4])));
                    i += 4;
                } else if (v == 48 && i + 4 < parts.length && "2".equals(parts[i+1])) {
                    StyleConstants.setBackground(attrs, new Color(
                            Integer.parseInt(parts[i+2]), Integer.parseInt(parts[i+3]), Integer.parseInt(parts[i+4])));
                    i += 4;
                }
            }
        }

        /**
         * Converte um número de cor ANSI básica (0 a 7: preto, vermelho, verde, amarelo/marrom,
         * azul, magenta, ciano, cinza) numa cor do Java. "bright" escolhe a versão brilhante.
         */
        private Color ansiColor(int i, boolean bright) {
            Color[] base = {
                    new Color(0,0,0), new Color(170,0,0), new Color(0,170,0), new Color(170,85,0),
                    new Color(0,0,170), new Color(170,0,170), new Color(0,170,170), new Color(170,170,170)
            };
            Color[] br = {
                    new Color(85,85,85), new Color(255,85,85), new Color(85,255,85), new Color(255,255,85),
                    new Color(85,85,255), new Color(255,85,255), new Color(85,255,255), new Color(255,255,255)
            };
            return (bright ? br : base)[i];
        }

        /**
         * Converte um número da paleta de 256 cores (usada em ESC[38;5;Nm) numa cor do Java:
         * 0 a 15 são as cores básicas; 16 a 231 formam um cubo de cores 6x6x6; 232 a 255 são
         * tons de cinza, do mais escuro ao mais claro.
         */
        private Color color256(int idx) {
            if (idx < 16)  return ansiColor(idx < 8 ? idx : idx - 8, idx >= 8);
            if (idx < 232) {
                int i = idx - 16;
                return new Color((i / 36) * 51, ((i % 36) / 6) * 51, (i % 6) * 51);
            }
            int g = (idx - 232) * 10 + 8;
            return new Color(g, g, g);
        }
    }

    // ------------------------------------------------------------------------------------
    // FONTE
    // ------------------------------------------------------------------------------------

    /**
     * Escolhe a fonte do painel. Só aceita fontes que realmente tenham o glifo do braille.
     * Tenta, nesta ordem:
     * <ol>
     *   <li>um arquivo .ttf dentro do projeto (classpath: pasta resources marcada como
     *       "Resources Root" no IntelliJ);</li>
     *   <li>um arquivo .ttf no disco, relativo à pasta onde o programa roda (no IntelliJ,
     *       a raiz do projeto): resources/NomeDaFonte.ttf ou NomeDaFonte.ttf;</li>
     *   <li>uma fonte já instalada no sistema (ex.: Cascadia Mono) que tenha braille;</li>
     *   <li>último recurso: a fonte genérica Monospaced (o braille pode virar quadrado).</li>
     * </ol>
     * O resultado fica guardado em FONTE_CACHE, então a busca só acontece uma vez. Os avisos
     * "[Text.ConsolePane] ..." aparecem no console do IntelliJ e dizem qual fonte foi usada.
     */
    private static Font carregarFonte() {
        if (FONTE_CACHE != null) return FONTE_CACHE;

        // 1) Recurso no classpath (várias posições)
        for (String p : new String[]{
                "/resources/CascadiaCode-Regular.ttf",
                "/CascadiaCode-Regular.ttf",
                "/resources/DejaVuSansMono.ttf",
                "/DejaVuSansMono.ttf",
                "/resources/IntelOneMono-Regular.ttf",
                "/IntelOneMono-Regular.ttf",
                "/resources/MapleMono-Regular.ttf",
                "/MapleMono-Regular.ttf"}) {
            try (InputStream is = ConsolePane.class.getResourceAsStream(p)) {
                if (is == null) continue;
                Font f = tentarCarregar(is.readAllBytes(), "classpath:" + p);
                if (f != null) return FONTE_CACHE = f;
            } catch (Exception e) {
                System.err.println("[Text.ConsolePane] Erro lendo " + p + ": " + e.getMessage());
            }
        }

        // 2) Arquivo no disco (CWD)
        for (String p : new String[]{
                "resources/CascadiaCode-Regular.ttf", "CascadiaCode-Regular.ttf",
                "resources/DejaVuSansMono.ttf", "DejaVuSansMono.ttf",
                "resources/IntelOneMono-Regular.ttf", "IntelOneMono-Regular.ttf",
                "resources/MapleMono-Regular.ttf", "MapleMono-Regular.ttf"}) {
            File f = new File(p);
            if (!f.exists()) continue;
            try {
                Font fonte = tentarCarregar(java.nio.file.Files.readAllBytes(f.toPath()),
                        "disco:" + f.getAbsolutePath());
                if (fonte != null) return FONTE_CACHE = fonte;
            } catch (Exception e) {
                System.err.println("[Text.ConsolePane] Erro lendo " + p + ": " + e.getMessage());
            }
        }

        // 3) Famílias instaladas no sistema que REALMENTE têm o glifo braille
        System.err.println("[Text.ConsolePane] Nenhum .ttf com braille encontrado. Procurando fontes instaladas...");
        String escolhida = null;
        for (String fam : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            // fontes lógicas dependem de fallback; só queremos fontes físicas
            if (fam.equals("Dialog") || fam.equals("DialogInput") || fam.equals("Monospaced")
                    || fam.equals("SansSerif") || fam.equals("Serif")) continue;
            if (!new Font(fam, Font.PLAIN, 16).canDisplay('\u283F')) continue;
            if (escolhida == null) escolhida = fam;
            String minusc = fam.toLowerCase();
            if (minusc.contains("mono") || minusc.contains("cascadia")) { escolhida = fam; break; }
        }
        if (escolhida != null) {
            System.err.println("[Text.ConsolePane] Usando fonte instalada: " + escolhida);
            return FONTE_CACHE = new Font(escolhida, Font.PLAIN, 16);
        }

        System.err.println("[Text.ConsolePane] AVISO: nenhuma fonte com braille. Braille aparecerá como quadrados.");
        return FONTE_CACHE = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    }

    /**
     * Tenta transformar os bytes de um arquivo de fonte numa Font do Java.
     *
     * <p>Passos: (1) cria a fonte a partir dos bytes (tenta TrueType e, se falhar, Type1);
     * (2) REGISTRA a fonte no sistema gráfico do Java (sem registrar, o Swing não a encontra
     * pelo nome); (3) pergunta se ela tem o glifo do braille (caractere U+283F); (4) se tiver,
     * devolve a fonte no tamanho 16; se não tiver, devolve null e o chamador tenta a próxima.</p>
     *
     * @param origem texto só para os avisos (de onde veio a fonte)
     */
    private static Font tentarCarregar(byte[] bytes, String origem) {
        // Tenta TTF, depois TYPE1 (cobre alguns .otf que são CFF disfarçados de TTF)
        Font base = null;
        try {
            base = Font.createFont(Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(bytes));
        } catch (FontFormatException | IOException e1) {
            try {
                base = Font.createFont(Font.TYPE1_FONT, new java.io.ByteArrayInputStream(bytes));
            } catch (FontFormatException | IOException e2) {
                System.err.println("[Text.ConsolePane] " + origem + " não é fonte válida: " + e2.getMessage());
                return null;
            }
        }

        // REGISTRA ANTES de checar/derivar
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);

        // Só aceita a fonte se ela REALMENTE tiver o glifo braille
        boolean temBraille = base.canDisplay('\u283F');
        System.err.println("[Text.ConsolePane] Carregada: " + origem +
                " | family=" + base.getFamily() +
                " | canDisplay(braille)=" + temBraille);

        if (!temBraille) {
            System.err.println("[Text.ConsolePane]   sem braille, ignorando " + origem);
            return null;
        }

        return base.deriveFont(Font.PLAIN, 16f);
    }

    /**
     * (NÃO É USADO por nenhum outro trecho.) Versão antiga e mais simples de tentarCarregar,
     * que ficou no arquivo. Pode ser apagada sem afetar o funcionamento.
     */
    private static Font processarFonte(Font base, String origem) {
        if (!base.canDisplay('\u283F')) {
            System.err.println("[Text.ConsolePane] " + origem + " carregou mas NAO tem glifo braille (family=" + base.getFamily() + ")");
            return null;
        }
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
        System.err.println("[Text.ConsolePane] OK: " + origem + " (family=" + base.getFamily() + ")");
        return base.deriveFont(Font.PLAIN, 16f);
    }
}
