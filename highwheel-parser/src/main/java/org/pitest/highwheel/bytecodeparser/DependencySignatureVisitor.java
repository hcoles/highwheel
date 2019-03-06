package org.pitest.highwheel.bytecodeparser;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;
import org.pitest.highwheel.classpath.AccessVisitor;
import org.pitest.highwheel.model.AccessPoint;
import org.pitest.highwheel.model.AccessType;
import org.pitest.highwheel.model.ElementName;

public class DependencySignatureVisitor extends SignatureVisitor {

  private final AccessPoint   parent;
  private final AccessVisitor typeReceiver;
  private final AccessType type;

  public DependencySignatureVisitor(final AccessPoint owner,
      final AccessVisitor typeReceiver, AccessType type) {
    super(Opcodes.ASM7);
    this.typeReceiver = typeReceiver;
    this.parent = owner;
    this.type = type;
  }

  @Override
  public void visitClassType(final String name) {
    this.typeReceiver.apply(this.parent,
        AccessPoint.create(ElementName.fromString(name)), type);

  }

}
