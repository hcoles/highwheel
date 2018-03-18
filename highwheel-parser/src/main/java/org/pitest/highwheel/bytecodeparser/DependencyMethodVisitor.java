package org.pitest.highwheel.bytecodeparser;

import static org.pitest.highwheel.bytecodeparser.NameUtil.getElementNameForType;

import org.objectweb.asm.*;
import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessPointName;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;

class DependencyMethodVisitor extends MethodVisitor {

  private final AccessPoint   parent;
  private final AccessVisitor typeReceiver;
  private final NameTransformer nameTransformer;

  public DependencyMethodVisitor(final AccessPoint owner,
      final AccessVisitor typeReceiver, NameTransformer nameTransformer) {
    super(Opcodes.ASM5, null);
    this.typeReceiver = typeReceiver;
    this.parent = owner;
    this.nameTransformer = nameTransformer;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    this.typeReceiver
        .apply(this.parent, AccessPoint.create(
            nameTransformer.transform(owner), AccessPointName.create(name, desc)),
            AccessType.USES);
  }

  @Override
  public void visitFieldInsn(final int opcode, final String owner,
      final String name, final String desc) {
    this.typeReceiver
        .apply(this.parent, AccessPoint.create(
            nameTransformer.transform(owner), AccessPointName.create(name, desc)),
            AccessType.USES);
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc,
      final boolean visible) {
    this.typeReceiver.apply(this.parent, AccessPoint.create(ElementName
        .fromString(org.objectweb.asm.Type.getType(desc).getClassName())),
        AccessType.ANNOTATED);
    return null;
  }

  @Override
  public AnnotationVisitor visitParameterAnnotation(final int parameter,
      final String desc, final boolean visible) {
    this.typeReceiver.apply(this.parent, AccessPoint.create(ElementName
        .fromString(org.objectweb.asm.Type.getType(desc).getClassName())),
        AccessType.ANNOTATED);
    return null;
  }

  @Override
  public void visitLdcInsn(final Object cst) {
    if (cst instanceof Type) {
      ElementName element = getElementNameForType((Type) cst);
      this.typeReceiver
      .apply(this.parent, AccessPoint.create(element),
          AccessType.USES);
    }
  }

  @Override
  public void visitLocalVariable(String name, String desc, String signature,
                                 Label start, Label end, int index) {
    final ElementName element = ElementName.fromString(org.objectweb.asm.Type.getType(desc).getClassName());
    this.typeReceiver.apply(this.parent,AccessPoint.create(element),AccessType.USES);
  }
}
