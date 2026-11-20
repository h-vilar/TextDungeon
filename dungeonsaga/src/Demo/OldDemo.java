package Demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class OldDemo {

    public static void demo(){

        final BufferedReader in =  new BufferedReader(new InputStreamReader(System.in));

        // evitar bugs do braille
        //System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Scanner teclado = new Scanner(System.in);

        // Códigos ANSI
        final String VERMELHO = "\u001B[31m"; // vermelho padrão
        final String LARANJA = "\u001B[38;5;208m"; // laranja (256 cores)
        final String AMARELO = "\u001B[33m"; // amarelo padrão
        final String VERDE = "\u001B[32m"; // verde padrão
        final String CIANO = "\u001B[36m"; // ciano padrão
        final String AZUL = "\u001B[34m"; // azul padrão
        final String ROXO = "\u001B[38;5;93m"; // roxo (256 cores)
        final String ROSA = "\u001B[38;5;213m"; //(256 cores)
        final String MAGENTA = "\u001B[35m"; // magenta padr
        final String VERMELHO_BRILHANTE = "\u001B[91m";
        final String AMARELO_BRILHANTE = "\u001B[93m";
        final String VERDE_BRILHANTE = "\u001B[92m";
        final String CIANO_BRILHANTE = "\u001B[96m";
        final String AZUL_BRILHANTE = "\u001B[94m";
        final String MAGENTA_BRILHANTE = "\u001B[95m";
        final String VERMELHO_ESCURO = "\u001B[38;5;88m"; // #870000
        final String LARANJA_ESCURO = "\u001B[38;5;130m"; // #AF5F00
        final String AMARELO_ESCURO = "\u001B[38;5;136m"; // #AF8700
        final String VERDE_ESCURO = "\u001B[38;5;22m"; // #005F00
        final String CIANO_ESCURO = "\u001B[38;5;23m"; // #005F5F
        final String AZUL_ESCURO = "\u001B[38;5;17m"; // #00005F
        final String ROXO_ESCURO = "\u001B[38;5;54m"; // #5F0087
        final String ROSA_ESCURO = "\u001B[38;5;89m"; // #87005F
        final String MAGENTA_ESCURO = "\u001B[38;5;53m"; // #5F005F
        final String NEGRITO = "\u001B[1m";
        final String OPCO_ESMAECIDO = "\u001B[2m";
        final String ITALICO = "\u001B[3m";
        final String SUBLINHADO = "\u001B[4m";
        final String INVERTER_CORES = "\u001B[7m";
        final String RESET = "\u001B[0m";

        final String FUNDO_PRETO   = "\u001B[40m";
        final String FUNDO_VERMELHO = "\u001B[41m";
        final String FUNDO_VERDE   = "\u001B[42m";
        final String FUNDO_AMARELO = "\u001B[43m";
        final String FUNDO_AZUL    = "\u001B[44m";
        final String FUNDO_MAGENTA = "\u001B[45m";
        final String FUNDO_CIANO   = "\u001B[46m";
        final String FUNDO_BRANCO  = "\u001B[47m";

        final String FUNDO_PRETO_BRILHANTE   = "\u001B[100m"; // Cinza escuro
        final String FUNDO_VERMELHO_BRILHANTE = "\u001B[101m";
        final String FUNDO_VERDE_BRILHANTE    = "\u001B[102m";
        final String FUNDO_AMARELO_BRILHANTE  = "\u001B[103m";
        final String FUNDO_AZUL_BRILHANTE     = "\u001B[104m";
        final String FUNDO_MAGENTA_BRILHANTE  = "\u001B[105m";
        final String FUNDO_CIANO_BRILHANTE    = "\u001B[106m";
        final String FUNDO_BRANCO_BRILHANTE   = "\u001B[107m"; // Branco puro

        final String FUNDO_LARANJA = "\u001B[48;5;208m";
        final String FUNDO_ROXO    = "\u001B[48;5;93m";
        final String FUNDO_ROSA    = "\u001B[48;5;213m";


        int hp = 200;
        final int hpmax = hp;

        String texto = VERDE_ESCURO+ "Surge da escuridão um DRAGÃO-KIRIN D'ÁGUA! Enorme e magicamente flutuante, ele paira sobre o ar depois de irromper da piscina funda presente no meio da câmara.. De repente, o dragão foca em um de vocês. A presença de um elementalista de fogo parece o deixar furioso...\n\n"+VERDE_BRILHANTE;
        String texto2 = "     ...Conforme ele carrega um tsunami. \n\n                 PREPARE-SE!\n";
        texto = texto + texto2;

        String med = VERDE+"  Cambaleando após irromper de uma rachadura na parede á frente, uma mulher estranha se aproxima. Ao se aproximar da luz, vocês notam que ela tem serpentes como cabelos. A criatura ri de olhos fechados parada, enquanto surge em suas mãos feixes verdes e estranhos de magia...\n\n";
        String med2 = VERDE_BRILHANTE+"   ...MEDUSA abre os olhos e levanta as mãos. \n\n                  DEFENDA-SE!\n\n";
        med= med+med2;

        // limpa console
        // System.out.print("\033[H\033[2J"); System.out.flush();

        // tira o cursor
        System.out.print("\u001B[?25l");


        while (hp > 0) {

            //"limpa tela"
            System.out.print("\033[H\033[2J"); System.out.flush();

            dragao(VERDE_BRILHANTE);
            //Enemies.KirinDragon.intro();
            // System.out.printf(" ⟨⟨%d/%d⟩⟩ \n\n", hp, hpmax);
            //texto pre ataque
            for (Character c : med.toCharArray()) {
                System.out.print(c);
                if (!Character.isWhitespace(c)) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {}
                }
            }

            // barra
            System.out.flush();
            for (int i = 0; i < 12; i++) {
                System.out.printf("\r ░▒▓███████████████████████████████▓▒░");
                System.out.flush();
                try {
                    Thread.sleep(80);
                } catch (Exception e) {}
                System.out.printf("\r                                            ");
                System.out.flush();
                try {
                    Thread.sleep(80);
                } catch (Exception e) {}
            }

            System.out.printf("\n\n\n");
            try {
                Thread.sleep(10);
            } catch (Exception e) {}

            // tsunami
            for (int i = 0; i < hp; i++) {
                System.out.printf("%s░%s░░░▒%s▓%s█%s▓%s▒░░░░░░░░%s░", VERDE, VERDE_ESCURO, VERDE_ESCURO,
                        VERDE_ESCURO, VERDE_ESCURO, VERDE_ESCURO, VERDE_ESCURO);
                try {
                    Thread.sleep(5);
                } catch (Exception e) {}
            }
            System.out.printf("\n\n\n\n\n\n\n\n\n\n"+MAGENTA_BRILHANTE);

            //quick time event lipe demo
            int qte=0;
            int hits=5;
            boolean hit=true;
            while(qte<5){
                for (int i = 0; i < 2; i++) {
                    System.out.printf("\r ░▒▓██████████████CRIT!██████████████▓▒░");
                    System.out.flush();
                    try {
                        Thread.sleep(60);
                    } catch (Exception e) {}
                    System.out.printf("\r                                            ");
                    System.out.flush();
                    try {
                        Thread.sleep(60);
                    } catch (Exception e) {}
                }


                if(hit){
                    System.out.printf("                            ★ ★ ★ HIT! %d\n\n\n%s", hits, AMARELO_BRILHANTE); hit=false;}
                else {System.out.printf("          %d Hit! •\n\n\n%s", hits, MAGENTA_BRILHANTE); hit=true; }
                teclado.nextLine();
                hits--;
                qte++;
            }

            System.out.print(RESET + "Dano:");
            hp -= teclado.nextInt();
        }


        System.out.printf("\nFinal");

    }

    public static void dragao(String cor) {
        System.out.println(cor);
        System.out.println("   ⠀   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⡗⢦⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("      ⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⣤⡤⠤⠶⠿⣶⣦⣄⣙⣦⠙⢿⡓⢦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠳⣄⠀⠀⠀⠭⣉⠓⠒⠚⠽⡄⢹⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀   ⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⣋⡿⠀⠀⠀⣀⡽⠀⠀⠀⠘⢈⠳⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣿⣷⡆⢠⠖⠚⣇⠀⣙⡶⠄⠈⠰⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀   ⠀⠀⠀⠀⠀⠀⠀⠀⣼⠁⠐⣧⣾⡄⣄⠘⣆⡈⠷⠄⣤⣶⠈⢿⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠀   ⠀⠀⠀⠀⠀⠘⠋⠛⣦⣘⢿⣇⣿⣿⠉⠹⣗⠦⣈⠘⣾⡿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢱⡾⠛⣏⠿⠧⡀⠈⠳⣄⡙⠊⠉⠛⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀   ⠀⠀⠀⠀⠀⢠⣤⡤⣤⡴⠉⠀⠀⠈⢳⡀⠑⠦⡀⠈⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀   ⠀⠀⠀⠀⣀⣀⣠⣬⠷⢮⣙⣒⣤⡤⠀⠀⢹⡄⡄⢱⠀⠀⠈⢳⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀   ⠀⠀⠀⠀⠈⠛⠳⣄⠀⠀⠀⠀⠠⣌⣩⠿⠀⠒⣼⡇⠇⠈⠳⡄⠀⠈⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀   ⠀⠀⠀⠀⠀⠀⠀⢈⣹⣤⣖⣸⣍⣿⠽⠤⠒⠚⠋⠀⠀⠀⠀⢸⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀   ⠀⠀⠀⠀⣠⠾⠛⠋⠉⠉⠀⠀⠀⠀⣠⡤⠂⠀⠀⢠⡀⢰⠋⠀⢠⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⠀⢀⣠⠤⣶⡾⠅⠀⠀⠀⠀⠀⠀⠀⠀⠸⡏⢺⠀⠀⠀⢈⣇⠜⠃⠀⣾⡈⢳⡀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⠀⢻⠇⡴⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣼⠋⠈⠀⠀⢠⠞⠋⠀⢀⡾⠁⠉⠀⢻⡄⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⢠⣏⡼⠁⠀⠀⠀⢀⡤⠤⠔⠊⢉⣡⠼⠓⠀⠀⢀⣴⣋⣀⡠⠶⠻⣄⡀⠀⠀⠈⡇⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⣸⡿⠁⢠⠔⠒⠚⢋⡤⠖⠊⢩⡟⠀⠀⢀⣠⠞⠋⠉⠁⠀⠀⠀⠀⠀⠉⢹⠀⠀⣷⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⢼⢻⠇⡤⠇⠀⢀⣴⠋⠀⠀⠀⣼⣠⠴⠚⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢧⣀⡟⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠙⣿⡰⠂⠀⢠⣿⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣿⠃⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⣿⡇⠀⠀⣏⢻⡀⢀⣠⠴⠛⠉⠓⠢⢄⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⠈⢿⣄⡀⠘⢆⡙⠋⠀⢀⡠⠔⠦⠐⠒⣚⠙⣲⡦⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⠀⠀⠙⢧⡀⠀⠈⠉⠉⠁⣀⠀⣀⡴⠚⠉⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⠀⠀⠀⠀⠙⣶⣤⡀⠉⠘⠛⠻⣅⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⠀⠀⠀⠀⠀⠈⠳⡌⠁⠒⢄⠀⠈⠑⢢⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("   ⠀⠀⠀⠀⠀⠀⠀⠀⠈⠳⠤⣄⣁⣀⣀⣀⣛⣲⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀ ");
        System.out.println();
    }

    public static void medusa(String cor) {
        System.out.println(cor);
        System.out.println("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠁⠷⢧⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢄⠀⠀⠀⠀⠀⢦⠀⠀⠀⠚⠉⠐⢪⣧⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⡀⠀⠀⠀⠐⢦⡘⠚⣣⠀⢰⡋⠠⠃⣰⠀⠀⠀⠀⡰⣟⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⢀⣴⡾⡝⢤⠀⠀⠀⡼⣧⢠⠃⠏⣷⠛⡡⣄⢸⣠⠄⣠⢶⡃⡼⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⣼⡧⠀⠂⠀⠀⣰⠲⠦⣉⡗⡾⡧⡗⡛⣖⠉⠋⠁⡾⡍⢇⢳⡑⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠐⠿⠇⣿⡯⡶⣶⢆⢀⣼⠃⡾⠁⠳⠜⠙⠮⠳⣀⠀⠣⢑⢮⠠⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠉⠉⠃⢰⡹⡎⡏⣞⢁⣝⣦⠀⠀⠀⢀⠰⢣⠘⡠⠀⢺⠀⠀⠀⠀⠀⠀⠀⡀⣀⣤⠤⡀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⢳⡀⢳⠇⡇⣸⠝⣮⣼⡀⣤⣾⣶⣿⢀⠈⠷⡄⠉⠀⠀⠀⠀⠀⢠⢾⢹⠧⣠⠦⠵⣀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠈⡞⡀⣧⠃⠈⣯⢿⣿⣿⢿⣿⣿⡏⢯⠀⡤⠁⠀⠀⠀⠀⢀⠴⢫⠳⡇⠡⠂⡀⢈⠀⣈⠑⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠐⠑⡸⣆⠈⢳⣟⣿⣿⡛⣿⢿⣭⠏⠀⡄⠀⠀⠀⢀⡀⠀⢌⡵⣦⡁⠀⠀⠀⠁⠛⢰⢫⢡⠀");
        System.out.println("⠀⠀⠀⠀⠀⡰⠧⠰⢹⠆⢀⠉⠂⠁⠐⡀⠌⠀⠀⠀⠀⠀⠠⣬⣥⠲⢌⢷⣦⣕⠳⡀⠀⠀⠀⠀⠀⠉⠲⠀");
        System.out.println("⠀⠀⠀⠀⢀⡏⣎⠬⠆⡇⡼⢄⡐⢌⠲⠒⠲⠘⡄⠈⠲⡀⠀⡈⢷⠻⣌⠳⣴⡹⠳⣦⢤⡀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⢏⠘⢜⠼⠸⣅⠳⣎⢮⣦⣛⠛⠌⣝⡹⣔⢧⡉⠐⠦⡀⠳⡌⠦⡈⢦⣄⠀⣉⠛⣦⡀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠙⢂⠻⠝⣫⡳⣭⣖⢌⣁⠭⣀⣴⣀⠀⠃⠵⡙⣤⠡⣘⢮⢃⠞⠱⠈⠳⠄⢱⢰⢃⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠘⣤⢉⢶⡐⣇⣘⢌⡭⢔⣪⡽⣾⡽⣷⠆⠀⠱⡘⠎⠏⠀⠉⠀⠀⢤⣠⠀⢠⣒⠃⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠀⢸⣉⠶⣝⡾⣽⢮⡴⣫⢾⣽⡷⣿⣹⡎⢦⠀⠀⢀⡀⠲⡤⢄⡀⣀⡸⢉⠷⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠀⠀⢹⣞⡳⣯⣏⣯⢶⠻⣟⣾⢛⣳⡼⡙⢄⣙⠠⢤⣚⢦⣜⢉⡚⣡⢗⣫⠇⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⢀⢀⠀⣞⢷⣛⣾⣳⢶⡹⢏⣷⢳⠾⠳⣥⢂⡝⠻⠆⠪⣳⣚⡬⢷⣩⢎⠏⠁⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⡤⢹⡜⠀⡸⢯⡽⡶⠶⣻⢀⡞⢌⣧⣶⡷⣤⠙⢬⡥⠐⠃⠐⢜⠽⠃⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⣈⠷⡮⣵⠀⣯⢳⣞⡞⠱⣡⡟⣨⢾⠇⡟⣧⡝⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠰⠏⣘⠳⠆⢻⣌⢷⣫⣜⣲⣩⡵⠞⣩⠏⠀⢌⢘⡯⢸⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⢸⣡⠛⣀⡆⠀⠹⡄⢄⠘⠒⠑⠘⠂⠁⠀⠀⠀⠈⠀⢼⣺⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠁⠀⣯⠃⠀⠀⠈⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢷⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⢸⠧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢺⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠈⠉⠂⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
        System.out.println("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠃⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");

    }

}