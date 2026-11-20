package MainGame;

import Enemies.Enemy;
import Enemies.KirinDragon;
import Player.Fairy;
import Player.Geni;
import Player.Jogador;
import Player.Sasquatch;
import Text.ConsolePane;
import Text.Cores;
import Text.TextDungeon;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static Scanner sc;
    private static void TextDungeon() {

        /* na hora de começar a batalha, um inimigo aleatorio da lista de inimigos vai ser
        selecionado. por isso que não pode adicionar o boss nessa lista; vai ter uma outra
        lista pra selecionar um dos bosses aleatoriamente na parte do gameloop que tiver
        que ser o boss.
         */
        ArrayList<Enemy> inimigos = new ArrayList<>();
        KirinDragon kirin = new KirinDragon();
        inimigos.add(kirin);

        ArrayList<Jogador> herois = new ArrayList<>();
        Fairy fada =       new Fairy(100, "Fada");
        Sasquatch pe = new Sasquatch(100, "Pe Grande");
        Geni g =            new Geni(100, "Geni");
        herois.add(fada);
        herois.add(pe);
        herois.add(g);

        // ^^^ pré configuração ^^^
        // texto e arte de intro

        //(deixar tudo amarelo pra depois ver as cores)
        System.out.println(Cores.AMARELO_intro);

        //antes/entre as batalhas, gostaria que o texto viesse lento
        //(uns 60 millis) para dar um contraste com a sensação de ação x calmaria.

        Historia.placeholder();
        TextDungeon.escrever(Historia.intro, 1);

        TextDungeon.escrever("   Player 1, escolha seu herói: \n\n\n", 10);
        for(int i = 0; i<herois.size(); i++){
            System.out.printf((i+1)+") ");
            TextDungeon.escrever(herois.get(i).nome, 10);
            System.out.printf("\n");
            TextDungeon.escrever(herois.get(i).sinopse, 10);
        }
        int n1 = opcao(1, herois.size());
        Jogador P1 = herois.remove(n1 - 1); // o jogador vê 1, 2, 3...; a lista começa em 0

        TextDungeon.escrever("\n   Player 2, escolha seu herói: \n\n\n", 10);
        for(int i = 0; i<herois.size(); i++){
            System.out.printf((i+1)+") ");
            TextDungeon.escrever(herois.get(i).nome, 10);
            System.out.printf("\n");
            TextDungeon.escrever(herois.get(i).sinopse, 10);
        }
        int n2 = opcao(1, herois.size());
        Jogador P2 = herois.remove(n2 - 1);

        TextDungeon.clean();

        Enemy Enemy1 = inimigos.remove(Jogo.criarNumeroAleatorio(inimigos.size()));

        boolean Batalha = true;
        boolean inicio = true;

        //começar ost aqui e...
        while(Batalha){
            System.out.flush();
            TextDungeon.clean();
            if(inicio) {
                Enemy1.Art(Cores.CIANO_BRILHANTE);
                Enemy1.intro(); inicio = false;
            } else {
                Enemy1.Art(Cores.AMARELO_BRILHANTE);
                //enemy state
                switch (Jogo.criarNumeroAleatorio(2)){
                    case 0:
                        Enemy1.state1(); break;
                    case 1:
                        Enemy1.state2(); break;
                    case 2:
                        Enemy1.state3(); break;
                }
            }

            P1.options();
            n1 = opcao(1, 5);
            switch (n1){
                case 1:
                    P1.habilidade1(Enemy1, P2);
                    break;
                case 2:
                    P1.habilidade2(Enemy1, P2);
                    break;
            }

            if(Enemy1.hp<=0){
                //...
                Enemy1.death();
                Batalha = false;
            }

            P2.options();
            n2 = opcao(1, 5);
            switch (n2){
                case 1:
                    P2.habilidade1(Enemy1, P1); break;
                case 2:
                    P2.habilidade2(Enemy1, P1); break;
            }

            if(Enemy1.hp<=0){
                //...
                Enemy1.death();
                Batalha = false;
            }

            //enemy atk
            switch (Jogo.criarNumeroAleatorio(2)){
                case 0:
                    Enemy1.ATTACK1(P1, P2); break;
                case 1:
                    Enemy1.ATTACK2(P1, P2); break;
                case 2:
                    Enemy1.ATTACK3(P1, P2); break;
            }

            if(P1.hp<=0&&P2.hp<=0){
                TextDungeon.GameOver();
            }

        }














        //Demo.Demo.demo(); //ainda to editando as coisas dentro do demo que é mais rapido,
        //mas já já ponho o real inicio aqui (classe historia.art, .intro, e aí o menu de escolha de personagens.)

        /*em algum lugar, vai ficar uma coisa tipo:
        (dentro da função de batalha)

        Text.TextDungeon.escrever(Enemies.Enemy.intro, Enemies.Enemy.drama);
         */

        System.out.printf("\n\n\nFim");
    }

    public static void main(String[] args) {

        //Text.TextDungeon(); //pra provar que o console pane é apenas um capricho e o jogo é o mesmo sem ele

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TextDungeon");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ConsolePane());
            frame.setSize(900, 1000);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Thread(Main::TextDungeon).start();
        });

    }

    /** Lê um número de min a max (inclusive). Repete até o jogador digitar algo válido. */
    public static int opcao(int min, int max) {
        if (sc == null) sc = new Scanner(System.in);   // criado só no primeiro uso
        while (true) {
            String linha = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(linha);
                if (n >= min && n <= max) {
                    System.out.flush();
                    return n;
                }
            } catch (NumberFormatException e) { }
            System.out.println("Escolha uma das opções! (de " + min + " a " + max + ".)");
        }
    }


    /*Inicialização: Com as classes base criadas, podemos programar o jogo! No início
    do programa, chame o mét0do iniciarJogo(). Esse mét0do deve definir o
    número de jogadores, que corresponde ao tamanho do ArrayList retornado. Em
    seguida, ainda no mét0do iniciarJogo(), exiba na tela uma mensagem de
    boas-vindas, apresentando o nome, idade, descrição e a regra modificada de cada
    personagem por meio do mét0do contarHistoria() definido na classe de cada
    Personagem. Por fim, permita que cada jogador escolha um personagem exclusivo,
    utilizando o mét0do setPersonagem(), e instancie cada personagem selecionado.

    Finalização: Quem ganhou? Determine o vencedor com base nos dados de
    pontuacao() e exiba a mensagem apropriada para cada possível final.*/
}