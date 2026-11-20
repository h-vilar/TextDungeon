package Enemies;

import Player.Jogador;

public class Medusa extends Enemy {

    public final int maxhp = 100;
    int hp = maxhp;

    long drama = 10; //tempo pras descrições desse inimigo escreverem.

    boolean Airborne = false; //se é aéreo ou está no ar agora~
    boolean Waterlog = false; //se está na água ou molhado agora~

    boolean Fiery = false;
    boolean Fragile = true; //+ afetado por tremor
    boolean Reflexive = false; //anti-projétil
    boolean Toxic = false; //anti-melee
    boolean AntiPhysic = false; //imune a dano físico
    boolean AntiMagic = false; //imune a dano mágico

    @Override
    public void ATTACK1(Jogador p1, Jogador p2){

    }
    @Override
    public void ATTACK2(Jogador p1, Jogador p2){

    }
    @Override
    public void ATTACK3(Jogador p1, Jogador p2){

    }

    @Override
    public void state1(){

    }
    @Override
    public void state2(){

    }
    @Override
    public void state3(){

    }

    @Override
    public void intro() {

    }

    @Override
    public void death() {

    }

    @Override
    public void Art(String cor) {

    }
}
