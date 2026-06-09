package com.erichiroshi.speechaihexagonal.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@SuppressWarnings("unused")
@AnalyzeClasses(packages = "com.erichiroshi.speechaihexagonal")
public class HexagonalArchitectureTest {

    // Globais (Usando caminhos mais precisos para evitar falsos positivos)
    private static final String ANY_DOMAIN         = "..domain..";
    private static final String ANY_APPLICATION    = "..application..";
    private static final String ANY_INFRASTRUCTURE = "..infrastructure..";
    private static final String ANY_EVENT          = "..domain.event..";
    private static final String ANY_EXCEPTION      = "..exception..";

    // Bounded Context: Transcription
    private static final String TRANSCRIPTION_DOMAIN = "..transcription.domain..";
    private static final String TRANSCRIPTION_APP    = "..transcription.application..";

    // Bounded Context: Analysis
    private static final String ANALYSIS_DOMAIN      = "..analysis.domain..";
    private static final String ANALYSIS_APP         = "..analysis.application..";

    // Bounded Context: Notification
    private static final String NOTIFICATION_DOMAIN  = "..notification.domain..";
    private static final String NOTIFICATION_APP     = "..notification.application..";

    // Frameworks externas
    private static final String PKG_SPRING      = "org.springframework..";
    private static final String PKG_JAKARTA_JPA = "jakarta.persistence..";
    private static final String PKG_JACKSON      = "com.fasterxml.jackson..";
    private static final String PKG_RESILIENCE4J = "io.github.resilience4j..";
    private static final String PKG_MICROMETER   = "io.micrometer..";
    private static final String PKG_SPRING_AI    = "org.springframework.ai..";
    private static final String PKG_REDIS        = "org.springframework.data.redis..";
    private static final String PKG_LOMBOK        = "lombok..";


    @ArchTest
    public static final ArchRule arquitetura_hexagonal_completa = onionArchitecture()
            .domainModels("..domain.model..", "..domain.event..", "..domain.exception..")
            .domainServices("..domain.service..")
            .applicationServices("..application..")
            .adapter("infra", ANY_INFRASTRUCTURE); // Unifica a proteção da infra completa

    // ── Isolamento Total do Domínio ─────────────────────────────────────────────

    @ArchTest
    public static final ArchRule dominio_deve_ser_java_puro = noClasses()
            .that().resideInAPackage(ANY_DOMAIN)
            .should().dependOnClassesThat().resideInAPackage(PKG_SPRING)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_JAKARTA_JPA)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_JACKSON)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_RESILIENCE4J)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_MICROMETER)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_SPRING_AI)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_LOMBOK)
            .because("O domínio é o núcleo do software. Deve ser Java puro e agnóstico a frameworks");

    @ArchTest
    public static final ArchRule dominio_nao_deve_depender_da_aplicacao =
            noClasses()
                    .that().resideInAPackage(ANY_DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ANY_APPLICATION)
                    .because("O domínio representa as regras de negócio e não pode conhecer casos de uso da aplicação");

    @ArchTest
    public static final ArchRule dominio_nao_deve_depender_da_infra =
            noClasses()
                    .that().resideInAPackage(ANY_DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ANY_INFRASTRUCTURE)
                    .because("O domínio deve ser independente de tecnologias externas e detalhes de infraestrutura");

    @ArchTest
    public static final ArchRule analysis_domain_nao_acessa_transcription_domain =
            noClasses()
                    .that().resideInAPackage(ANALYSIS_DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(TRANSCRIPTION_DOMAIN)
                    .because("Bounded contexts devem manter isolamento de domínio e não compartilhar regras de negócio diretamente");

    @ArchTest
    public static final ArchRule transcription_domain_nao_acessa_analysis_domain =
            noClasses()
                    .that().resideInAPackage(TRANSCRIPTION_DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ANALYSIS_DOMAIN)
                    .because("Cada bounded context deve evoluir de forma independente sem acoplamento ao domínio de outro contexto");

    // ── Cobertura da Camada de Aplicação ─────────────────────────────────

    @ArchTest
    public static final ArchRule application_nao_deve_depender_de_infraestrutura = noClasses()
            .that().resideInAPackage(ANY_APPLICATION)
            .should().dependOnClassesThat().resideInAPackage(ANY_INFRASTRUCTURE)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_JAKARTA_JPA)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_REDIS)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_RESILIENCE4J)
            .orShould().dependOnClassesThat().resideInAPackage(PKG_SPRING_AI)
            .because("Use cases dependem exclusivamente de portas (interfaces), nunca de tecnologias de infraestrutura");

    // Isolamento entre Bounded Contexts ──

    @ArchTest
    public static final ArchRule analysis_nao_acessa_dominio_de_transcription = noClasses()
            .that().resideInAPackage(ANALYSIS_APP)
            .should().dependOnClassesThat().resideInAPackage(TRANSCRIPTION_DOMAIN)
            .because("Bounded contexts não se acoplam via domínio — integração somente via portas na infra");

    @ArchTest
    public static final ArchRule transcription_nao_acessa_dominio_de_analysis = noClasses()
            .that().resideInAPackage(TRANSCRIPTION_APP)
            .should().dependOnClassesThat().resideInAPackage(ANALYSIS_DOMAIN)
            .because("O contexto de transcrição não deve conhecer as regras de negócio de análise");

    // ── Convenções de Nomenclatura e Posicionamento (Design Clássico) ──────────────────

    @ArchTest
    public static final ArchRule portas_de_entrada_devem_terminar_com_port = classes()
            .that().resideInAPackage("..port.in..")
            .should().haveSimpleNameEndingWith("Port")
            .because("Driver ports (port/in) devem expor contratos com sufixo 'Port'");

    @ArchTest
    public static final ArchRule portas_de_saida_devem_terminar_com_port = classes()
            .that().resideInAPackage("..port.out..")
            .should().haveSimpleNameEndingWith("Port")
            .because("Driven ports (port/out) devem expor contratos com sufixo 'Port'");

    @ArchTest
    public static final ArchRule use_cases_devem_ser_services_com_sufixo = classes()
            .that().resideInAPackage(ANY_APPLICATION)
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beAnnotatedWith(Service.class)
            .because("Use cases orquestram fluxos e precisam ser gerenciados como beans pelo Spring");

    @ArchTest
    public static final ArchRule entidades_jpa_e_controllers_devem_morar_na_infra = classes()
            .that().areAnnotatedWith(Entity.class)
            .or().areAnnotatedWith(RestController.class)
            .should().resideInAPackage(ANY_INFRASTRUCTURE)
            .because("Entidades de banco de dados e controladores HTTP são detalhes puros de infraestrutura");

    @ArchTest
    public static final ArchRule adapters_devem_morar_na_infraestrutura = classes()
            .that().haveSimpleNameEndingWith("Adapter")
            .should().resideInAPackage(ANY_INFRASTRUCTURE)
            .because("Classes com sufixo Adapter servem para plugar a infraestrutura nas portas da aplicação");

    // ── Portas devem ser interfaces ──────────────────

    @ArchTest
    public static final ArchRule portas_devem_ser_interfaces =
            classes()
                    .that().resideInAPackage("..port..")
                    .should().beInterfaces()
                    .because("Portas definem contratos da aplicação e devem ser representadas por interfaces");

    @ArchTest
    public static final ArchRule portas_nao_devem_ser_anotadas_com_service =
            noClasses()
                    .that().resideInAPackage("..port..")
                    .should().beAnnotatedWith(Service.class)
                    .because("Portas representam contratos e não devem ser registradas como componentes Spring");

    @ArchTest
    public static final ArchRule use_cases_devem_ser_classes =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("UseCase")
                    .should()
                    .notBeInterfaces()
                    .because("Use cases representam implementações concretas da aplicação e não contratos");

    // ── Controllers ──────────────────

    @ArchTest
    public static final ArchRule controllers_nao_devem_acessar_adapters =
            noClasses()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .dependOnClassesThat()
                    .haveSimpleNameEndingWith("Adapter")
                    .because("Controllers devem depender apenas de portas da aplicação e nunca de implementações de infraestrutura");

    @ArchTest
    public static final ArchRule controllers_nao_devem_acessar_repositories =
            noClasses()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .dependOnClassesThat()
                    .haveSimpleNameEndingWith("Repository")
                    .because("Controllers não devem acessar persistência diretamente; toda orquestração deve passar pela aplicação");

    @ArchTest
    public static final ArchRule controllers_devem_acessar_apenas_aplicacao_dominio_e_pacotes_http =
            classes()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            ANY_APPLICATION,
                            ANY_DOMAIN,
                            "..infrastructure.http..",
                            "java..",
                            PKG_SPRING,
                            PKG_LOMBOK
                    )
                    .because("Controllers devem apenas orquestrar a entrada HTTP, mapear DTOs e chamar a aplicação/domínio");

    @ArchTest
    public static final ArchRule controllers_nao_devem_depender_de_usecases_concretos =
            noClasses()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .dependOnClassesThat()
                    .haveSimpleNameEndingWith("UseCase")
                    .because("Controllers devem depender de portas de entrada e não de implementações concretas de casos de uso");

    @ArchTest
    public static final ArchRule controllers_nao_devem_depender_de_frameworks =
            noClasses()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            PKG_JAKARTA_JPA,
                            PKG_REDIS,
                            PKG_SPRING_AI,
                            PKG_RESILIENCE4J
                    )
                    .because("Controllers devem delegar responsabilidades para a aplicação e não acessar tecnologias diretamente");

    // ── Eventos ──────────────────
    @ArchTest
    public static final ArchRule eventos_devem_terminar_com_event =
        classes()
            .that()
            .resideInAPackage(ANY_EVENT)
            .should()
            .haveSimpleNameEndingWith("Event")
            .because("Eventos de domínio devem seguir uma nomenclatura consistente para facilitar identificação e manutenção");


    // ── Exceptions ──────────────────

    @ArchTest
    public static final ArchRule exceptions_devem_terminar_com_exception =
            classes()
                    .that()
                    .resideInAPackage(ANY_EXCEPTION)
                    .should()
                    .haveSimpleNameEndingWith("Exception")
                    .because("Exceções devem possuir nomenclatura padronizada para facilitar entendimento e tratamento");

    // ── Repositórios ──────────────────

    @ArchTest
    public static final ArchRule repositories_devem_ficar_na_infra =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Repository")
                    .should()
                    .resideInAPackage(ANY_INFRASTRUCTURE)
                    .because("Repositórios são detalhes de persistência e pertencem exclusivamente à infraestrutura");

    // ── Bounded context notification/ ────────────────────────────────────────

    @ArchTest
    public static final ArchRule notification_domain_nao_deve_depender_de_spring =
            noClasses().that().resideInAPackage(NOTIFICATION_DOMAIN)
                    .should().dependOnClassesThat().resideInAPackage(PKG_SPRING)
                    .because("O domínio de notification deve ser Java puro — sem Spring");

    @ArchTest
    public static final ArchRule notification_application_nao_deve_depender_de_infraestrutura =
            noClasses().that().resideInAPackage(NOTIFICATION_APP)
                    .should().dependOnClassesThat().resideInAPackage("..notification.infrastructure..")
                    .because("Use cases de notification dependem apenas de portas — nunca de adapters");

    @ArchTest
    public static final ArchRule notification_application_nao_importa_transcription_domain =
            noClasses().that().resideInAPackage(NOTIFICATION_APP)
                    .should().dependOnClassesThat().resideInAPackage(TRANSCRIPTION_DOMAIN)
                    .because("Bounded contexts se integram via portas na infraestrutura — nunca via domínio diretamente");

    @ArchTest
    public static final ArchRule notification_domain_nao_importa_outros_dominios =
            noClasses().that().resideInAPackage(NOTIFICATION_DOMAIN)
                    .should().dependOnClassesThat().resideInAPackage(TRANSCRIPTION_DOMAIN)
                    .orShould().dependOnClassesThat().resideInAPackage(ANALYSIS_DOMAIN)
                    .because("Domínios de bounded contexts distintos não se acoplam diretamente");

    // ── Documentação OpenAPI ──────────────────────────────────────────────────

    @ArchTest
    public static final ArchRule interfaces_de_documentacao_devem_residir_no_pacote_documentation =
            classes().that().haveSimpleNameEndingWith("Documentation")
                    .should().resideInAPackage("..documentation..")
                    .because("Interfaces de documentação OpenAPI devem ficar em pacote documentation/ " +
                            "para manter controllers livres de anotações Swagger");

    @ArchTest
    public static final ArchRule controllers_devem_implementar_interface_de_documentacao =
            classes().that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .should().implement(com.tngtech.archunit.base.DescribedPredicate.<com.tngtech.archunit.core.domain.JavaClass>describe(
                            "interface de documentação",
                            interfaceClass -> interfaceClass.getSimpleName().contains("Documentation")))
                    .because("Todo @RestController deve implementar uma interface de documentação " +
                            "para manter separação entre lógica HTTP e anotações OpenAPI");

}
