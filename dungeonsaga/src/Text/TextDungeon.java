package Text;

import MainGame.Jogo;
import Player.Jogador;

import java.util.ArrayList;

public class TextDungeon implements Jogo {

    public static void escrever(String texto, long millis){ //maquina de escrever
        for (Character c : texto.toCharArray()) {
            System.out.print(c);
            if (!Character.isWhitespace(c)) {
                try {
                    Thread.sleep(millis); //5,10,20,30 são bons
                } catch (Exception _) {}
            }
        }
    }

    public static void clean() {
        System.out.print("\033[H\033[2J"); System.out.flush();}

    public static void GameOver(){

    }
































    @Override
    public void jogarTurno(Jogador jogador) {
    }

    @Override
    public ArrayList<Jogador> iniciarjogo() {
        return null;
    }

    @Override
    public boolean jogoAcabou() {
        return false;
    }

    //ughhhhhh.
    void pontuacao(){}

}
