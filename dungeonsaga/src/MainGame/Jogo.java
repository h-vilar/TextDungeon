package MainGame;

import Player.Jogador;

import java.util.ArrayList;
import java.util.Random;

public interface Jogo {

    //i. Apresenta as regras do jogo aos jogadores.
    //ii. Inicializa e retorna a lista de jogadores participantes.
    ArrayList<Jogador> iniciarjogo();

    //i. Executa a jogada do Player.Jogador especificado como parâmetro.
    //ii. Gerencia toda a lógica relacionada à vez do jogador (ex.:
    //        movimentos, ações, decisões).
    void jogarTurno(Jogador jogador);

    //i. Verifica se o jogo atingiu uma condição de término.
    //ii. Retorna true se o jogo acabou, ou false caso contrário.
    boolean jogoAcabou();

    //i. Gera e retorna um número aleatório estaticamente.
    //ii. Deve ser usado para criar eventos randômicos que aumentam o
    //dinamismo do jogo [...]
    static int criarNumeroAleatorio(int length){
        Random r = new Random();
        return r.nextInt(0, length);
    }

}
