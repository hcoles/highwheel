package org.pitest.highwheel.bytecodeparser;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.Type;
import org.pitest.highwheel.bytecodeparser.classpath.ClassLoaderClassPathRoot;
import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.classpath.ClasspathRoot;
import org.pitest.highwheel.cycles.Filter;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;
import org.pitest.highwheel.model.AccessPointName;

import com.example.AnException;
import com.example.AnInterface;
import com.example.CallsFooMethod;
import com.example.ConstructsAFoo;
import com.example.DeclaresAnException;
import com.example.ExtendsFoo;
import com.example.Foo;
import com.example.HasArrayOfFooAsMember;
import com.example.HasFooArrayAsParameter;
import com.example.HasFooAsMember;
import com.example.HasFooAsParameter;
import com.example.HasMainMethod;
import com.example.ImplementsAnInterface;
import com.example.ReturnsAFoo;
import com.example.ReturnsArrayOfFoo;
import com.example.Unconnected;
import com.example.UsesFieldOnFoo;
import com.example.annotated.AnAnnotation;
import com.example.annotated.AnnotatedAtClassLevel;
import com.example.annotated.AnnotatedAtFieldLevel;
import com.example.annotated.AnnotatedAtMethodLevel;
import com.example.annotated.AnnotatedAtParameterLevel;
import com.example.annotated.AnnotatedAtVariableLevel;
import com.example.classliterals.HasFieldOfTypeClassFoo;
import com.example.classliterals.MethodAccessFooClassLiteral;
import com.example.classliterals.StoresFooArrayClassLiteralAsField;
import com.example.classliterals.StoresFooClassLiteralAsField;
import com.example.generics.BoundedByFoo;
import com.example.generics.HasCollectionOfFooParameter;
import com.example.generics.ImplementsGenericisedInterface;
import com.example.generics.ReturnsCollectionOfFoo;
import com.example.innerclasses.CallsMethodFromFooWithinInnerClass;

public class ClassPathParserSystemTest {

  private ClassPathParser testee;

  @Mock
  private AccessVisitor   v;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldDetectAnInheritanceDepedencyWhenOneClassExtendsAnother() {
    parseClassPath(ExtendsFoo.class, Foo.class);
    verify(this.v).apply(accessAType(ExtendsFoo.class), accessAType(Foo.class),
        AccessType.INHERITANCE);
  }
  
  @Test
  public void shouldDetectAnImplementsDepedencyWhenClassImplementsInterface() {
    parseClassPath(ImplementsAnInterface.class, AnInterface.class);
    verify(this.v).apply(accessAType(ImplementsAnInterface.class), accessAType(AnInterface.class),
        AccessType.IMPLEMENTS);
  }

  @Test
  public void shouldDetectACompositionDependencyWhenClassIncludesAnother() {
    parseClassPath(HasFooAsMember.class, Foo.class);
    verify(this.v).apply(accessAType(HasFooAsMember.class),
        accessAType(Foo.class), AccessType.COMPOSED);
  }

  @Test
  public void shouldDetectACompositionDependencyWhenClassIncludesArrayField() {
    parseClassPath(HasArrayOfFooAsMember.class, Foo.class);
    verify(this.v).apply(accessAType(HasArrayOfFooAsMember.class),
        accessAType(Foo.class), AccessType.COMPOSED);
  }
  
  @Test
  public void shouldDetectSignatureDependencyWhenMethodReturnsAType() {
    parseClassPath(ReturnsAFoo.class, Foo.class);
    verify(this.v).apply(access(ReturnsAFoo.class, method("foo", Foo.class)),
        accessAType(Foo.class), AccessType.SIGNATURE);
  }


  @Test
  public void shouldDetectACompositionDependencyWhenClassReturnsAnArray() {
    parseClassPath(ReturnsArrayOfFoo.class, Foo.class);
    verify(this.v).apply(access(ReturnsArrayOfFoo.class, method("foo", Foo[].class)),
        accessAType(Foo.class), AccessType.SIGNATURE);
  }
  
  @Test
  public void shouldDetectSignatureDependencyWhenMethodHasParameterOfType() {
    parseClassPath(HasFooAsParameter.class, Foo.class);
    verify(this.v).apply(access(HasFooAsParameter.class, methodWithParameter("foo", Foo.class)),
        accessAType(Foo.class), AccessType.SIGNATURE);
  }
  
  @Test
  public void shouldDetectSignatureDependencyWhenMethodHasArrayParameter() {
    parseClassPath(HasFooArrayAsParameter.class, Foo.class);
    verify(this.v).apply(access(HasFooArrayAsParameter.class, methodWithParameter("foo", Foo[].class)),
        accessAType(Foo.class), AccessType.SIGNATURE);
  }
  
  @Test
  public void shouldDetectASignatureDependencyWhenDeclaresAnException() {
    parseClassPath(DeclaresAnException.class, AnException.class);
    verify(this.v).apply(access(DeclaresAnException.class, method("foo", "()V")),
        accessAType(AnException.class), AccessType.SIGNATURE);
  }

  @Test
  public void shouldDetectUsesDependencyWhenConstructsAType() {
    parseClassPath(ConstructsAFoo.class, Foo.class);
    verify(this.v).apply(access(ConstructsAFoo.class, method("foo",Object.class)),
        access(Foo.class, method("<init>","()V")), AccessType.USES);
  }

  @Test
  public void shouldDetectUsesDependencyWhenCallsMethodOnType() {
    parseClassPath(CallsFooMethod.class, Foo.class);
    verify(this.v).apply(access(CallsFooMethod.class, method("foo",Object.class)),
        access(Foo.class, method("aMethod",Object.class)), AccessType.USES);
  }

  @Test
  public void shouldDetectWhenAnnotatedAtClassLevel() {
    parseClassPath(AnnotatedAtClassLevel.class, AnAnnotation.class);
    verify(this.v).apply(accessAType(AnnotatedAtClassLevel.class),
        accessAType(AnAnnotation.class), AccessType.ANNOTATED);
  }

  @Test
  public void shouldDetectWhenAnnotatedAtMethodLevel() {
    parseClassPath(AnnotatedAtMethodLevel.class, AnAnnotation.class);
    verify(this.v).apply(access(AnnotatedAtMethodLevel.class, method("foo","()V")),
        accessAType(AnAnnotation.class), AccessType.ANNOTATED);
  }

  @Test
  public void shouldDetectWhenAnnotatedAtParameterLevel() {
    parseClassPath(AnnotatedAtParameterLevel.class, AnAnnotation.class);
    verify(this.v).apply(access(AnnotatedAtParameterLevel.class, method("foo","(I)V")),
        accessAType(AnAnnotation.class), AccessType.ANNOTATED);
  }

  @Test
  public void shouldDetectWhenAnnotatedAtFieldLevel() {
    parseClassPath(AnnotatedAtFieldLevel.class, AnAnnotation.class);
    verify(this.v).apply(accessAType(AnnotatedAtFieldLevel.class),
        accessAType(AnAnnotation.class), AccessType.ANNOTATED);
  }

  @Test
  public void willNotDetectWhenAnnotatedAtVariableLevel() {
    parseClassPath(AnnotatedAtVariableLevel.class, AnAnnotation.class);
    verify(this.v, never()).apply(accessAType(AnnotatedAtVariableLevel.class),
        accessAType(AnAnnotation.class), AccessType.ANNOTATED);
  }

  @Test
  public void shouldDetectAUsesRelationshipForParentClassMethodWhenNestedClassCallsMethod() {
    parseClassPath(CallsMethodFromFooWithinInnerClass.class, Foo.class);
    verify(this.v).apply(
        access(CallsMethodFromFooWithinInnerClass.class, method("foo","()V")),
        access(Foo.class, method("aMethod",Object.class)), AccessType.USES);
  }
  
  @Test
  public void shouldDetectAUsesRealtionshipWhenWritesToClassField() {
    parseClassPath(UsesFieldOnFoo.class, Foo.class);
    verify(this.v).apply(
        access(UsesFieldOnFoo.class, method("foo","()V")),
        access(Foo.class, method("aField","I")), AccessType.USES);
  }
  
  @Test
  public void shouldDetectAUsesRealtionshipWhenStoresClassLiteralAsField() {
    parseClassPath(StoresFooClassLiteralAsField.class, Foo.class);
    verify(this.v).apply(
        access(StoresFooClassLiteralAsField.class, method("<init>","()V")),
        accessAType(Foo.class), AccessType.USES);
  }
  
  @Test
  public void shouldDetectAUsesRealtionshipWhenStoresClassArrayLiteralAsField() {
    parseClassPath(StoresFooArrayClassLiteralAsField.class, Foo.class);
    verify(this.v).apply(
        access(StoresFooArrayClassLiteralAsField.class, method("<init>","()V")),
        accessAType(Foo.class), AccessType.USES);
  }
  
  @Test
  public void shouldDetectAUsesRelationshipWhenUsesFooClassLiteralInMethod() {
    parseClassPath(MethodAccessFooClassLiteral.class, Foo.class);
    verify(this.v).apply(
        access(MethodAccessFooClassLiteral.class, method("foo",Class.class)),
        accessAType(Foo.class), AccessType.USES);
  }
  
  @Test
  public void shouldDetectCompositionRelationshipWhenDeclaresFieldOfClassFoo() {
    parseClassPath(HasFieldOfTypeClassFoo.class, Foo.class);
    verify(this.v).apply(
        accessAType(HasFieldOfTypeClassFoo.class),
        accessAType(Foo.class), AccessType.COMPOSED);
  }
  
  @Test
  public void shouldDetectSignatureRelationshipWhenImplementsInterfaceParameterisedByFoo() {
    parseClassPath(ImplementsGenericisedInterface.class, Foo.class);
    verify(this.v).apply(
        accessAType(ImplementsGenericisedInterface.class),
        accessAType(Foo.class), AccessType.SIGNATURE);
    
  }
  
  @Test
  public void shouldDetectSignatureRelationshipWhenReturnsCollectionOfFoo() {
    parseClassPath(ReturnsCollectionOfFoo.class, Foo.class);
    verify(this.v).apply(
        accessAType(ReturnsCollectionOfFoo.class),
        accessAType(Foo.class), AccessType.SIGNATURE);
    
  }
  
  @Test
  public void shouldDetectSignatureRelationshipWhenHasCollectionOfFooParameter() {
    parseClassPath(HasCollectionOfFooParameter.class, Foo.class);
    verify(this.v).apply(
        accessAType(HasCollectionOfFooParameter.class),
        accessAType(Foo.class), AccessType.SIGNATURE);
    
  }
  
  @Test
  public void shouldDetectSignatureRelationshipWhenBoundedByFoo() {
    parseClassPath(BoundedByFoo.class, Foo.class);
    verify(this.v).apply(
        accessAType(BoundedByFoo.class),
        accessAType(Foo.class), AccessType.SIGNATURE);
    
  }
  
  @Test
  public void shouldDetectUnConnectedClasses() {
    parseClassPath(Unconnected.class);
    verify(this.v).newNode(ElementName.fromClass(Unconnected.class));
  }
  
  @Test
  public void shouldDetectEntryPointsInClassesWithMainMethod() {
    parseClassPath(HasMainMethod.class);
    verify(this.v).newEntryPoint(ElementName.fromClass(HasMainMethod.class));
  }
  
  @Test
  public void shouldNotDetectEntryPointsInClassesWithoutMainMethod() {
    parseClassPath(Foo.class);
    verify(this.v, never()).newEntryPoint(any(ElementName.class));
  }

  private Filter matchOnlyExampleDotCom() {
    return new Filter() {

      public boolean include(final ElementName item) {
        return item.asJavaName().startsWith("com.example");
      }

    };
  }

  private void parseClassPath(final Class<?>... classes) {
    try {
      this.testee = makeToSeeOnlyExampleDotCom();
      this.testee.parse(createRootFor(classes),this.v);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  private ClassPathParser makeToSeeOnlyExampleDotCom() {
	  return new ClassPathParser(matchOnlyExampleDotCom());
  }


  private ClasspathRoot createRootFor(final Class<?>[] classes) {
    final Collection<ElementName> elements = new ArrayList<ElementName>();
    final ClassLoaderClassPathRoot data = new ClassLoaderClassPathRoot(Thread
        .currentThread().getContextClassLoader());

    for (final Class<?> each : classes) {
      final ElementName element = ElementName.fromClass(each);
      elements.add(element);
      elements.addAll(first3InnerClassesIfPresent(element, data));
    }

    return new ClasspathRoot() {
      public InputStream getData(final ElementName name) throws IOException {
        return data.getData(name);
      }

      public Collection<ElementName> classNames() {
        return elements;
      }

      public InputStream getResource(final String name) throws IOException {
        return data.getResource(name);
      }

    };

  }

  private Collection<? extends ElementName> first3InnerClassesIfPresent(
      final ElementName element, final ClassLoaderClassPathRoot data) {
    final Collection<ElementName> innerClasses = new ArrayList<ElementName>();
    try {
      for (int i = 1; i != 4; i++) {
        final ElementName innerClass = ElementName.fromString(element
            .asJavaName() + "$" + i);
        if (data.getData(innerClass) != null) {
          innerClasses.add(innerClass);
        }
      }
      return innerClasses;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private AccessPoint accessAType(final Class<?> type) {
    return AccessPoint.create(ElementName.fromClass(type));
  }

  
  private AccessPoint access(final Class<?> type, final AccessPointName method) {
    return AccessPoint.create(ElementName.fromClass(type), method);
  }

  private AccessPointName methodWithParameter(String name, Class<?> paramType) {
    return AccessPointName.create(name, Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(paramType)));
  }
  
  private AccessPointName method(String name, String desc) {
    return AccessPointName.create(name, desc);
  }
  
  private AccessPointName method(String name, Class<?> retType) {
    return AccessPointName.create(name, Type.getMethodDescriptor(Type.getType(retType)));
  }
  
}
