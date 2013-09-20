package org.pitest.highwheel.algorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.pitest.highwheel.model.ElementName;

public class Cycle<V> implements Iterable<V> {

  private final Set<V> members;

  public Cycle(final Collection<V> members) {
    this.members = new HashSet<V>(members);
  }

  public boolean contains(final ElementName element) {
    return this.members.contains(element);
  }

  public int size() {
    return this.members.size();
  }

  public Iterator<V> iterator() {
    return this.members.iterator();
  }

  @Override
  public String toString() {
    return "Cycle [members=" + this.members + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.members == null) ? 0 : this.members.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Cycle<?> other = (Cycle<?>) obj;
    if (this.members == null) {
      if (other.members != null) {
        return false;
      }
    } else if (!this.members.equals(other.members)) {
      return false;
    }
    return true;
  }

}
