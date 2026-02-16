package com.zhangzhankui.seed.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 全局架构测试 - 使用 ArchUnit 确保代码架构合规
 *
 * <p>此测试类用于验证整个项目的架构规范，包括： - 分层架构规则 - 命名规范 - 依赖规则 - 循环依赖检测 - 安全规则
 *
 * <p>注意：模块级别的架构测试请使用 ArchitectureComplianceTest
 *
 * @see com.zhangzhankui.seed.system.architecture.ArchitectureComplianceTest
 */
@DisplayName("全局架构合规性测试")
@Tag("architecture")
class ArchitectureTest {

  private static JavaClasses classes;

  @BeforeAll
  static void setUp() {
    classes =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.zhangzhankui.seed");
  }

  @Nested
  @DisplayName("分层架构规则")
  class LayeredArchitectureTests {

    @Test
    @DisplayName("应遵循分层架构 - Controller -> Service -> Mapper")
    void shouldFollowLayeredArchitecture() {
      layeredArchitecture()
          .consideringAllDependencies()
          .layer("Controller")
          .definedBy("..controller..")
          .layer("Service")
          .definedBy("..service..")
          .layer("Mapper")
          .definedBy("..mapper..")
          .layer("Converter")
          .definedBy("..converter..")
          .layer("Domain")
          .definedBy("..domain..", "..entity..")
          .whereLayer("Controller")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Service")
          .mayOnlyBeAccessedByLayers("Controller", "Service")
          .whereLayer("Mapper")
          .mayOnlyBeAccessedByLayers("Service")
          .whereLayer("Converter")
          .mayOnlyBeAccessedByLayers("Controller", "Service")
          .check(classes);
    }
  }

  @Nested
  @DisplayName("命名规范")
  class NamingConventionTests {

    @Test
    @DisplayName("Controller 类应以 Controller 结尾")
    void controllersShouldEndWithController() {
      classes()
          .that()
          .resideInAPackage("..controller..")
          .and()
          .areNotAnonymousClasses()
          .and()
          .doNotHaveSimpleName("BaseController")
          .should()
          .haveSimpleNameEndingWith("Controller")
          .check(classes);
    }

    @Test
    @DisplayName("Service 实现类应以 ServiceImpl 结尾")
    void serviceImplsShouldEndWithServiceImpl() {
      classes()
          .that()
          .resideInAPackage("..service..")
          .and()
          .areNotInterfaces()
          .and()
          .haveSimpleNameContaining("Service")
          .should()
          .haveSimpleNameEndingWith("ServiceImpl")
          .check(classes);
    }

    @Test
    @DisplayName("Mapper 接口应以 Mapper 结尾")
    void mappersShouldEndWithMapper() {
      classes()
          .that()
          .resideInAPackage("..mapper..")
          .should()
          .haveSimpleNameEndingWith("Mapper")
          .check(classes);
    }
  }

  @Nested
  @DisplayName("依赖规则")
  class DependencyTests {

    @Test
    @DisplayName("Controller 不应直接依赖 Mapper（数据库访问层）")
    void controllersShouldNotDependOnMappers() {
      noClasses()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..mapper..")
          .andShould()
          .haveSimpleNameEndingWith("Mapper") // 只限制数据库
          // Mapper，不限制
          // Converter
          .check(classes);
    }

    @Test
    @DisplayName("Domain 类不应依赖 Service 或 Controller")
    void domainShouldNotDependOnUpperLayers() {
      noClasses()
          .that()
          .resideInAnyPackage("..domain..", "..entity..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..service..", "..controller..")
          .check(classes);
    }

    @Test
    @DisplayName("Common 模块不应依赖业务模块")
    void commonShouldNotDependOnBusinessModules() {
      // 只检查是否依赖具体的业务模块包，不包括 common.core.auth 这种内部包
      noClasses()
          .that()
          .resideInAPackage("..common..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.zhangzhankui.seed.system..", "com.zhangzhankui.seed.auth..")
          .check(classes);
    }
  }

  @Nested
  @DisplayName("循环依赖检测")
  class CyclicDependencyTests {

    @Test
    @DisplayName("包之间不应存在循环依赖")
    void shouldNotHaveCyclicDependencies() {
      slices().matching("com.zhangzhankui.seed.(*)..").should().beFreeOfCycles().check(classes);
    }
  }

  @Nested
  @DisplayName("注解使用规则")
  class AnnotationTests {

    @Test
    @DisplayName("Controller 必须使用 @RestController 注解")
    void controllersShouldBeAnnotatedWithRestController() {
      classes()
          .that()
          .resideInAPackage("..controller..")
          .and()
          .haveSimpleNameEndingWith("Controller")
          .and()
          .doNotHaveSimpleName("BaseController")
          .should()
          .beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
          .check(classes);
    }

    @Test
    @DisplayName("Service 实现类必须使用 @Service 注解")
    void servicesShouldBeAnnotatedWithService() {
      classes()
          .that()
          .resideInAPackage("..service..")
          .and()
          .haveSimpleNameEndingWith("ServiceImpl")
          .should()
          .beAnnotatedWith(org.springframework.stereotype.Service.class)
          .check(classes);
    }
  }

  @Nested
  @DisplayName("安全规则")
  class SecurityTests {

    @Test
    @DisplayName("不应在代码中硬编码密码")
    void shouldNotHaveHardcodedPasswords() {
      noFields()
          .that()
          .haveNameMatching(".*[Pp]assword.*")
          .should()
          .beStatic()
          .andShould()
          .beFinal()
          .because("密码不应硬编码在代码中")
          .check(classes);
    }

    @Test
    @DisplayName("不应使用 System.out/err（应使用日志框架）")
    void shouldNotUseSystemOut() {
      noClasses()
          .that()
          .resideInAnyPackage("..controller..", "..service..", "..mapper..")
          .should()
          .accessClassesThat()
          .belongToAnyOf(System.class)
          .because("应使用 SLF4J 而非 System.out/err")
          .allowEmptyShould(true)
          .check(classes);
    }
  }

  @Nested
  @DisplayName("可测试性规则")
  class TestabilityTests {

    @Test
    @DisplayName("Service 实现类不应是 final（需可 Mock）")
    void servicesShouldNotBeFinal() {
      noClasses()
          .that()
          .haveSimpleNameEndingWith("ServiceImpl")
          .should()
          .haveModifier(com.tngtech.archunit.core.domain.JavaModifier.FINAL)
          .because("需要能够被 Mock")
          .check(classes);
    }

    @Test
    @DisplayName("应使用构造器注入而非字段注入")
    void shouldPreferConstructorInjection() {
      fields()
          .that()
          .areDeclaredInClassesThat()
          .resideInAPackage("..service..")
          .should()
          .notBeAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
          .because("推荐使用构造器注入以提高可测试性")
          .allowEmptyShould(true)
          .check(classes);
    }
  }
}
