package Enemies;

import Player.Jogador;

public abstract class Enemy {

    public final int maxhp = 100;
    public int hp = maxhp;

    long drama = 10; //tempo pras descrições desse inimigo escreverem.

    boolean Airborne; //se é aéreo ou está no ar agora~
    boolean Waterlog; //se está na água ou molhado agora~

    boolean Fiery;
    boolean Fragile; //+ afetado por tremor
    boolean Reflexive; //anti-projétil
    boolean Toxic; //anti-melee
    boolean AntiPhysic; //imune a dano físico
    boolean AntiMagic; //imune a dano mágico

    public abstract void intro();
    public abstract void death();
    public abstract void Art(String cor);

    public abstract void ATTACK1(Jogador p1, Jogador p2);
    public abstract void ATTACK2(Jogador p1, Jogador p2);
    public abstract void ATTACK3(Jogador p1, Jogador p2);

    public abstract void state1();
    public abstract void state2();
    public abstract void state3();
}
