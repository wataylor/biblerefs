/* @name PortugeseZondervan.java

    Copyright (c) 2009 by Advanced Systems and Software Technologies.
    All Rights Reserved

    Under revision by: $Locker$
    Change Log:
    $Log: $
*/

package convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Arrays;

import utils.ConvUtils;

/**
 *
 * @author Material Gain
 * @version %I%, %G%
 * @since
 *
 * @see <classname>
 */

public class PortugeseZondervan {

  public static final long serialVersionUID = 1;

  /** This code is used in several .htm files to set the language.
   * Since it appears in several places, putting it here saves effort
   * in changing it.*/
  public static final String LANG_CODE = "pt";

  /** Similarly, defining the directory here simplifies changes.*/
  public static final String OUT_DIR = "PortugeseProd";

  public static NumberFormat digFormat = NumberFormat.getIntegerInstance();
  static {
    digFormat.setMinimumIntegerDigits(2);
  }

  /** Obligatory constructor.*/
  public PortugeseZondervan() { /* */ }

  public class WantDotHTML implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return name.endsWith(".xml");
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    File file;
    PrintStream ps;
    String fileName = null;
    String bookName;
    String[] fileNames;
    StringBuilder sb = new StringBuilder(2000);	// Holds entire files
    StringBuilder outString = new StringBuilder(2000);
    StringBuilder tocString = new StringBuilder(2000);
    int argno;
    int idx;

    /* Initialize the table of contents*/
    tocString.append(ConvUtils.TOC_HEADER);
    tocString = ConvUtils.replaceAll(tocString, "_LN_", LANG_CODE);
    tocString = ConvUtils.replaceAll(tocString, "_TITLE_", "Digital Bible");
    tocString = ConvUtils.replaceAll(tocString, "_HIGHLIGHT_", "Buscar nêste livro:");
    tocString = ConvUtils.replaceAll(tocString, "_BUTTON_", "Buscar");
    tocString.append("<b>Velho Testamento</b><br />\n");

    try {
      file = new File("./");
      fileNames = file.list(new PortugeseZondervan().new WantDotHTML());
      Arrays.sort(fileNames);
      for (argno  = 0; argno < fileNames.length; argno++) {
	fileName  = fileNames[argno];
	System.out.println(fileName);

	sb = ConvUtils.slurpFile(fileName, sb);
	outString.setLength(0);
	idx = sb.indexOf("<h4>");
	if (idx < 0) {
	  idx = sb.indexOf("<H4>");
	  if (idx < 0) {
	    System.err.println("No h4 in " + fileName);
	    continue;
	  }
	}
	sb.delete(0, idx);
	idx = sb.lastIndexOf("]]>");
	if (idx < 0) {
	  System.err.println("No ]]> in " + fileName);
	  continue;
	}
	sb.delete(idx, sb.length());

	/* Have one book of the Bible as a long text string, extract
	 * its name for the table of contents.  The first H4 is of the
	 * form book name space 1.*/

	idx = sb.indexOf(" 1");
	if (idx < 0) {
	  System.err.println("No name chapter 1 in " + fileName);
	  continue;
	}
	bookName = sb.substring(4, idx);
	tocString.append("<a target=\"right_frame\" href=\"" +
			 digFormat.format(argno+1) +
			 ".htm\">" + bookName + "</a><br />\n");
	if (argno == 38) {
	  tocString.append("<br /><b>Novo Testamento</b><br />\n");
	}

	/* There is a unicode character which does not convert properly without
	 * assistance which it precedes the character s.*/
	char ch = (char)65533;
    String tgt = String.valueOf(ch) + "s";

    /*
	idx = sb.indexOf("luz dia, e") + 9;
	String foo = sb.substring(idx, idx+4);
	for (int i=0; i<5; i++) {
		System.out.print((int)sb.charAt(idx + i) + " ");
	}
	System.out.println(idx + " >" + foo + "<");
	idx = sb.indexOf(tgt);
	foo = sb.substring(idx-1, idx+4);
	System.out.println(idx + " >" + foo + "<");
*/

    sb = ConvUtils.replaceAll(sb, tgt, "as");

	sb = ConvUtils.replaceAll(sb, "&nbsp;<sup", "\n<p><sup");
	sb = ConvUtils.replaceAll(sb, "&nbsp;&nbsp;&nbsp;  <sup", "\n<p><sup");
	sb = ConvUtils.replaceAll(sb, "&nbsp;&nbsp;&nbsp;  ]]>", "");
	//sb = ConvUtils.replaceAll(sb, "<![CDATA[ ", "");
	sb = ConvUtils.replaceAll(sb, " ]]>", "");
	//idx = sb.indexOf("<copyright");
	idx = sb.indexOf("</passage_text");
	if (idx > 0) {
	  sb.setLength(idx);
	}

	outString.append(ConvUtils.BOOK_HEADER);
	outString = ConvUtils.replaceAll(outString, "_LN_", LANG_CODE);
	outString = ConvUtils.replaceAll(outString, "_TITLE_", bookName);
	outString.append(sb);
	outString.append(ConvUtils.BOOK_TRAILER);

	file = new File(fileName = "../" + OUT_DIR + "/" + LANG_CODE + "/" +
			digFormat.format(argno+1) +
			".htm");
	if (!file.canWrite()) {
	  System.err.println("Can't write " + fileName);
	}
	ps = new PrintStream(file);
	ps.print(outString);
	ps.close();

      }

      /* Write the toc*/
      tocString.append(ConvUtils.TOC_TRAILER);
      file = new File(fileName = "../" + OUT_DIR + "/" + LANG_CODE + "/" +
		      "toc.htm");
      if (!file.canWrite()) {
	System.err.println("Can't write " + fileName);
      }
      ps = new PrintStream(file);
      ps.print(tocString);
      ps.close();

    } catch (FileNotFoundException e) {
      System.err.println(fileName + " is not found, " + e.toString());
    } catch (IOException e) {
      System.err.println(fileName + " io error, " + e.toString());
      e.printStackTrace();
    }
  }
}
