package br.com.safefield.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("OPERADOR")
public class Operador extends Usuario {

    @Enumerated(EnumType.STRING)
    private Turno turno;

    private String funcao;

    @ManyToOne(fetch = FetchType.LAZY)
    private AreaIndustrial area;

    protected Operador() {}

    public Operador(String nome, String matricula, String email, String senhaHash,
                    Turno turno, String funcao, AreaIndustrial area) {
        super(nome, matricula, email, senhaHash);
        this.turno = turno;
        this.funcao = funcao;
        this.area = area;
    }

    @Override
    public Perfil getPerfil() { return Perfil.OPERADOR; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }
    public String getFuncao() { return funcao; }
    public void setFuncao(String funcao) { this.funcao = funcao; }
    public AreaIndustrial getArea() { return area; }
    public void setArea(AreaIndustrial area) { this.area = area; }
}
