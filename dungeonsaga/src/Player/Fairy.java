package Player;

import Enemies.Enemy;
import Text.Cores;
import Text.TextDungeon;

public class Fairy extends Jogador {

    public Fairy(int maxhp, String nome){
        this.nome = nome;
        this.maxhp = maxhp;
        this.hp = maxhp;
        sinopse = "uma fadinha que voa\n\n";

    }

    //apresenta as características dos personagens
    //(nome, descrição, idade) e qual regra modifica.
    // (antigo contarHistoria()


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
        TextDungeon.escrever("\n\n  " + Cores.VERDE_BRILHANTE + "+10HP!", 10);
        aliado.hp += 10;
    }

    @Override
    public void habilidade2(Enemy enemy, Jogador aliado) {
        TextDungeon.escrever("\n\n  " + Cores.VERMELHO_BRILHANTE + "-90HP!", 10);
        enemy.hp -= 90;
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
