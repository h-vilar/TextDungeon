package Player;

import Enemies.Enemy;

public abstract class Jogador {

    int maxhp;
    public int hp;
    public String sinopse;
    public String nome;
    //escolhe o personagem de
    //cada jogador, onde idPersonagem indica o id do personagem. Se um
    //personagem já foi escolhido, os outros jogadores não podem escolher
    //o mesmo personagem.
    public void setPersonagem(int idPersonagem){

    }

    public abstract void habilidade1(Enemy enemy, Jogador aliado);
    public abstract void habilidade2(Enemy enemy, Jogador aliado);
    public abstract void habilidade3(Enemy enemy, Jogador aliado);
    public abstract void habilidade4(Enemy enemy, Jogador aliado);
    public abstract void habilidade5(Enemy enemy, Jogador aliado);







    //métod para que o jogador defina a
    //estratégia (jogada) a ser utilizada de acordo com as condições atuais
    //do jogo.
    public void pensarEstrategia(){

    }

    //méthodo para aplicar a estratégia escolhida
    //no méthodo anterior, atualizando itens como ficaria o tabuleiro ou o
    //estado do outro jogador.
    public void aplicarEstrategia(){

    }


    public abstract void options();
}
