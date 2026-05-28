package com.erichiroshi.speechaihexagonal.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * Testes de arquitetura hexagonal via ArchUnit.
 *
 * <p>Garante em tempo de build que as regras de isolamento entre camadas
 * nunca sejam violadas — independente de code review.
 *
 * <p>Estrutura verificada:
 * <pre>
 * domain/          → Java puro (sem Spring, sem JPA, sem Jackson)
 * application/     → depende apenas de domain/
 * infrastructure/  → depende de application/ e domain/; nunca o contrário
 * </pre>
 */
@SuppressWarnings("unused") // Remove os alertas de "never used" gerados pela IDE
@AnalyzeClasses(
        packages = "com.erichiroshi.speechaihexagonal"
)
public class HexagonalArchitectureTest {

    // 1. O CORAÇÃO DO TESTE: Valida tod_o o isolamento de acessos entre camadas
    @ArchTest
    public static final ArchRule arquitetura_hexagonal_completa = onionArchitecture()
            .domainModels("..domain.model..")
            .domainServices("..domain.service..")
            .applicationServices("..application..")
            .adapter("cache", "..infrastructure.cache..")
            .adapter("http", "..infrastructure.http..")
            .adapter("persistence", "..infrastructure.persistence..")
            .adapter("speechtotext", "..infrastructure.speechtotext..")
            .adapter("llm", "..infrastructure.llm..")
            .adapter("metrics", "..infrastructure.metrics..");

    // 2. CONVENÇÕES: Regras de sufixo e localização que o Onion não valida sozinho
    @ArchTest
    public static final ArchRule portas_de_entrada_devem_terminar_com_port =
            classes().that().resideInAPackage("..port.in..")
                    .should().haveSimpleNameEndingWith("Port");

    @ArchTest
    public static final ArchRule portas_de_saida_devem_terminar_com_port =
            classes().that().resideInAPackage("..port.out..")
                    .should().haveSimpleNameEndingWith("Port");

    @ArchTest
    public static final ArchRule adapters_devem_residir_na_infraestrutura =
            classes().that().haveSimpleNameEndingWith("Adapter")
                    .should().resideInAPackage("..infrastructure..");

    @ArchTest
    public static final ArchRule entidades_jpa_devem_residir_na_infraestrutura =
            classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                    .should().resideInAPackage("..infrastructure..");
}
