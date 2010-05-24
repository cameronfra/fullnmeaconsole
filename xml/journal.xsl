<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [
 <!ENTITY copy    "&#169;">
 <!ENTITY deg     "&#176;">
 <!ENTITY plusmn  "&#177;">
 <!ENTITY Delta   "&#916;">
 <!ENTITY delta   "&#948;">
 <!ENTITY epsilon "&#949;">
 <!ENTITY psi     "&#968;">
 <!ENTITY micro   "&#181;">
 <!ENTITY pi      "&#960;">
 <!ENTITY Pi      "&#928;">
 <!ENTITY frac12  "&#189;">
 <!ENTITY Agrave  "&#192;">
 <!ENTITY Aacute  "&#193;">
 <!ENTITY Acirc   "&#194;">
 <!ENTITY Atilde  "&#195;">
 <!ENTITY Auml    "&#196;">
 <!ENTITY Aring   "&#197;">
 <!ENTITY AElig   "&#198;">
 <!ENTITY Ccedil  "&#199;">
 <!ENTITY Egrave  "&#200;">
 <!ENTITY Eacute  "&#201;">
 <!ENTITY Ecirc   "&#202;">
 <!ENTITY Euml    "&#203;">
 <!ENTITY Igrave  "&#204;">
 <!ENTITY Iacute  "&#205;">
 <!ENTITY Icirc   "&#206;">
 <!ENTITY Iuml    "&#207;">
 <!ENTITY ETH     "&#208;">
 <!ENTITY Ntilde  "&#209;">
 <!ENTITY Ograve  "&#210;">
 <!ENTITY Oacute  "&#211;">
 <!ENTITY Ocirc   "&#212;">
 <!ENTITY Otilde  "&#213;">
 <!ENTITY Ouml    "&#214;">
 <!ENTITY times   "&#215;">
 <!ENTITY Oslash  "&#216;">
 <!ENTITY Ugrave  "&#217;">
 <!ENTITY Uacute  "&#218;">
 <!ENTITY Ucirc   "&#219;">
 <!ENTITY Uuml    "&#220;">
 <!ENTITY Yacute  "&#221;">
 <!ENTITY THORN   "&#222;">
 <!ENTITY szlig   "&#223;">
 <!ENTITY agrave  "&#224;">
 <!ENTITY aacute  "&#225;">
 <!ENTITY acirc   "&#226;">
 <!ENTITY atilde  "&#227;">
 <!ENTITY auml    "&#228;">
 <!ENTITY aring   "&#229;">
 <!ENTITY aelig   "&#230;">
 <!ENTITY ccedil  "&#231;">
 <!ENTITY egrave  "&#232;">
 <!ENTITY eacute  "&#233;">
 <!ENTITY ecirc   "&#234;">
 <!ENTITY euml    "&#235;">
 <!ENTITY igrave  "&#236;">
 <!ENTITY iacute  "&#237;">
 <!ENTITY icirc   "&#238;">
 <!ENTITY iuml    "&#239;">
 <!ENTITY eth     "&#240;">
 <!ENTITY ntilde  "&#241;">
 <!ENTITY ograve  "&#242;">
 <!ENTITY oacute  "&#243;">
 <!ENTITY ocirc   "&#244;">
 <!ENTITY otilde  "&#245;">
 <!ENTITY ouml    "&#246;">
 <!ENTITY divide  "&#247;">
 <!ENTITY oslash  "&#248;">
 <!ENTITY ugrave  "&#249;">
 <!ENTITY uacute  "&#250;">
 <!ENTITY ucirc   "&#251;">
 <!ENTITY uuml    "&#252;">
 <!ENTITY yacute  "&#253;"> 
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:fox="http://xml.apache.org/fop/extensions" 
                xmlns:geom-util="http://www.oracle.com/XSL/Transform/java/user.util.GeomUtil"
                xmlns:j-string="http://www.oracle.com/XSL/Transform/java/java.lang.String"
                xmlns:xsl-util="http://www.oracle.com/XSL/Transform/java/nauticalalmanac.xsl.XSLUtil"
                exclude-result-prefixes="data j-string xsl-util geom-util"
                version="2.0">

  <xsl:variable name="GEOMUTIL.HTML"   select="0"/>
  <xsl:variable name="GEOMUTIL.SHELL"  select="1"/>
  <xsl:variable name="GEOMUTIL.SWING"  select="2"/>
  <xsl:variable name="GEOMUTIL.NO_DEG" select="3"/>

  <xsl:variable name="GEOMUTIL.NONE" select="0"/>
  <xsl:variable name="GEOMUTIL.NS"   select="1"/>
  <xsl:variable name="GEOMUTIL.EW"   select="2"/>
  
  <xsl:variable name="GEOMUTIL.LEADING_SIGN"  select="0"/>
  <xsl:variable name="GEOMUTIL.TRAILING_SIGN" select="1"/>

  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="portrait-page"
                               page-width="8.5in" 
                               page-height="11in"> <!-- Portrait -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="landscape-page" 
                               page-height="8.5in" 
                               page-width="11in"> <!-- Portrait -->
          <fo:region-body margin="0in"/>
          <fo:region-after region-name="footer" extent="20mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="portrait-page"> <!-- landscape-page or portrait-page -->
        <fo:static-content flow-name="footer">
          <!--fo:block text-align="center">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block-->
          <fo:block text-align="center" font-size="8pt">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:block>
        </fo:static-content>
        <fo:flow flow-name="xsl-region-body">
          <!--fo:block break-after="page">
            <fo:block text-align="center" font-family="Book Antiqua" font-size="40pt" font-weight="bold" margin="0.5in">
              Log Book
            </fo:block>
            <fo:block>
              <fo:block text-align="left" font-family="Arial" font-size="8pt" font-style="italic">
                &copy; Oliv Cool Stuff Soft.
              </fo:block>
            </fo:block>
          </fo:block-->
          <!-- Data go here -->
          <fo:block margin="0.1in">
            <xsl:for-each select="/journal/day">
              <fo:block text-align="center" font-family="Courier" font-size="8pt" break-after="page" margin="0.45in">
                <fo:block text-align="left" font-size="12pt"><xsl:value-of select="./@date"/></fo:block>               
                <fo:table>
                  <fo:table-column column-width="1in"/>    <!-- Date -->
                  <fo:table-column column-width="1.5in"/>  <!-- Comment -->
                  <fo:table-column column-width="0.55in"/> <!-- HDG -->
                  <fo:table-column column-width="0.55in"/> <!-- LOG -->
                  <fo:table-column column-width="0.55in"/> <!-- BEAUFORT -->
                  <fo:table-column column-width="0.55in"/> <!-- TWS -->
                  <fo:table-column column-width="0.55in"/> <!-- TWD -->
                  <fo:table-column column-width="2in"/>    <!-- LAT/LONG -->
                  <fo:table-body>
                    <fo:table-row>
                      <fo:table-cell number-rows-spanned="2" border="0.5pt solid black" vertical-align="middle"><fo:block text-align="center">Time</fo:block></fo:table-cell>
                      <fo:table-cell number-rows-spanned="2" border="0.5pt solid black" vertical-align="middle"><fo:block text-align="center">Comment</fo:block></fo:table-cell>
                      <fo:table-cell number-rows-spanned="2" border="0.5pt solid black" vertical-align="middle"><fo:block text-align="center">Heading</fo:block></fo:table-cell>
                      <fo:table-cell number-rows-spanned="2" border="0.5pt solid black" vertical-align="middle"><fo:block text-align="center">Log</fo:block></fo:table-cell>
                      <fo:table-cell number-columns-spanned="3" border="0.5pt solid black"><fo:block text-align="center">Wind</fo:block></fo:table-cell>
                      <fo:table-cell number-rows-spanned="2" border="0.5pt solid black" vertical-align="middle"><fo:block text-align="center">Position</fo:block></fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                      <fo:table-cell border="0.5pt solid black"><fo:block text-align="center">Force</fo:block></fo:table-cell>
                      <fo:table-cell border="0.5pt solid black"><fo:block text-align="center">Speed</fo:block></fo:table-cell>
                      <fo:table-cell border="0.5pt solid black"><fo:block text-align="center">Dir</fo:block></fo:table-cell>
                    </fo:table-row>
                    <xsl:for-each select="./time">
                      <fo:table-row>
                        <xsl:variable name="date"><xsl:value-of select="./@value"/></xsl:variable>
                        <fo:table-cell border="0.5pt solid black"><fo:block text-align="center"><xsl:value-of select="substring-before(substring-after($date, 'T'), '.')"/></fo:block></fo:table-cell>
                        <fo:table-cell border="0.5pt solid black"><fo:block text-align="left"><xsl:value-of select="./comment"/></fo:block></fo:table-cell>
                        <fo:table-cell border="0.5pt solid black"><fo:block text-align="center"><xsl:value-of select="xsl-util:formatI3(./data[./@id='HDG'])"/></fo:block></fo:table-cell>
                        <fo:table-cell border="0.5pt solid black"><fo:block text-align="center"><xsl:value-of select="xsl-util:formatX2(./data[./@id='LOG'])"/></fo:block></fo:table-cell>
                        <fo:table-cell border="0.5pt solid black"><fo:block text-align="center"><xsl:value-of select="xsl-util:formatI1(./data[./@id='BEAUFORT'])"/></fo:block></fo:table-cell>
                        <fo:table-cell border="0.5pt solid black"><fo:block text-align="center"><xsl:value-of select="xsl-util:formatX2(./data[./@id='TWS'])"/></fo:block></fo:table-cell>
                        <fo:table-cell border="0.5pt solid black"><fo:block text-align="center"><xsl:value-of select="xsl-util:formatI3(./data[./@id='TWD'])"/></fo:block></fo:table-cell>
                        <fo:table-cell border="0.5pt solid black">
                          <fo:block text-align="center">
                            <xsl:value-of select="xsl-util:decToSex(./data[./@id='LAT'], $GEOMUTIL.SWING, $GEOMUTIL.NS, $GEOMUTIL.LEADING_SIGN)" disable-output-escaping="yes"/> - <xsl:value-of select="xsl-util:decToSex(./data[./@id='LNG'], $GEOMUTIL.SWING, $GEOMUTIL.EW, $GEOMUTIL.LEADING_SIGN)" disable-output-escaping="yes"/>
                          </fo:block>
                        </fo:table-cell>
                      </fo:table-row>
                    </xsl:for-each>
                  </fo:table-body>
                </fo:table>
              </fo:block>
            </xsl:for-each>
          </fo:block>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
</xsl:stylesheet>
