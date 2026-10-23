package br.com.safefield.domain;

import jakarta.persistence.*;

/** Usuário do sistema. Herança em tabela única, como no diagrama de classes da Sprint 1. */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "perfil", discriminatorType = DiscriminatorType.STRING)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senhaHash;

    private boolean ativo = true;

    protected Usuario() {}

    protected Usuario(String nome, String matricula, String email, String senhaHash) {
        this.nome = nome;
        this.matricula = matricula;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public abstract Perfil getPerfil();

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMatricula() { return matricula; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
