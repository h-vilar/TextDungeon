package Player;

import Enemies.Enemy;

public class Geni extends Jogador {

    final int maxhp;
    int hp;

    public Geni(int maxhp, String nome){
        this.nome = nome;
        this.maxhp = maxhp;
        this.hp = maxhp;
        sinopse = "um genio\n\n";

    }

    //apresenta as características dos personagens
    //(nome, descrição, idade) e qual regra modifica.
    void contarHistoria(){

    }

    //mét0dos para reescrever a regra a
    //ser aplicada para o Player.Jogador em questão.
    void adicionaRegra(){

    }
    void modificaRegra() {

    }
    void sorteiaRegra(){

    }

    @Override
    public void habilidade1(Enemy enemy, Jogador aliado) {

    }

    @Override
    public void habilidade2(Enemy enemy, Jogador aliado) {

    }

    @Override
    public void habilidade3(Enemy enemy, Jogador aliado) {

    }

    @Override
    public void habilidade4(Enemy enemy, Jogador aliado) {

    }

    @Override
    public void habilidade5(Enemy enemy, Jogador aliado) {

    }

    @Override
    public void options() {

    }
}
