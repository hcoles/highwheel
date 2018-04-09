package org.pitest.highwheel.util;

public abstract class GlobToRegex {

  public static String convertGlobToRegex(final String glob) {
    final StringBuilder out = new StringBuilder("^");
    for (int i = 0; i < glob.length(); ++i) {
      final char c = glob.charAt(i);
      switch (c) {
      case '$':
        out.append("\\$");
        break;
      case '*':
        out.append(".*");
        break;
      case '?':
        out.append('.');
        break;
      case '.':
        out.append("\\.");
        break;
      case '\\':
        out.append("\\\\");
        break;
      default:
        out.append(c);
      }
    }
    out.append('$');
    return out.toString();
  }

}
