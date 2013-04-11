<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  PMD Style sheet. Version 1.4
  Authored by arthurguru (with lots of inspiration from what came before).

  This version supports two extra optional parameters:
    title: Inserts a title at the top of the html page.
      Often set to the project name and version or buildno.
    filepathheader: Most projects often have long pathnames to get to the 
      source code, this parameter strips the leading path of
      a matching pathname to keep the width of the report manageable.
      Often set to ${buildir} or ${basedir}.

    ANT Usage:
      <xslt in="foo.xml" style="ag-pmd-report.xslt" out="foo.html">
        <param name="title" expression="${build.name}"/>
        <param name="filepathheader" expression="${basedir}"/>
      </xslt>

  Enjoy.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes" />
    <xsl:decimal-format decimal-separator="." grouping-separator="," />

    <xsl:param name="title" />
    <xsl:param name="filepathheader" />

    <xsl:template name="message">
        <xsl:value-of disable-output-escaping="yes" select="." />
    </xsl:template>

    <xsl:template name="priorityDiv">
        <xsl:if test="@priority = 1">p1</xsl:if>
        <xsl:if test="@priority = 2">p2</xsl:if>
        <xsl:if test="@priority = 3">p3</xsl:if>
        <xsl:if test="@priority = 4">p4</xsl:if>
        <xsl:if test="@priority = 5">p5</xsl:if>
    </xsl:template>

    <xsl:template name="timestamp">
        <xsl:value-of select="substring-before(substring-after(//pmd/@timestamp, 'T'), '.')" /> -
        <xsl:value-of select="substring-before(//pmd/@timestamp, 'T')" />
    </xsl:template>

    <xsl:template match="pmd">
        <html>
            <head>
                <title>PMD Report -
                    <xsl:value-of select="$title" />
                </title>
                <style type="text/css">
                    body {
                    margin-left: 10;
                    margin-right: 10;
                    font:normal 80% arial,helvetica,sanserif;
                    background-color:#FFFFFF;
                    color:#000000;
                    }
                    .a td { background: #efefef; }
                    .b td { background: #fff; }
                    th, td { text-align: left; vertical-align: top; }
                    th { font-weight:bold; background: #ccc; color: black; }
                    table, th, td { font-size:100%; border: none }
                    table.log tr td, tr th { }
                    h2 {
                    font-weight:bold;
                    font-size:120%;
                    margin-top: 2;
                    margin-bottom: 2;
                    }
                    h3 {
                    font-size:100%;
                    font-weight:bold;
                    background: #525D76;
                    color: white;
                    text-decoration: none;
                    padding: 5px;
                    margin-right: 2px;
                    margin-left: 2px;
                    margin-bottom: 0;
                    }
                    .p1 { text-align:center; color:#000000; background:#FF4444; }
                    .p2 { text-align:center; color:#000000; background:#FF9999; }
                    .p3 { text-align:center; color:#000000; background:#FFFF99; }
                    .p4 { text-align:center; color:#000000; background:#DCFF20; }
                    .p5 { text-align:center; color:#000000; background:#33CC33; }
                </style>
            </head>
            <body>
                <a name="top"></a>
                <hr />
                <!-- Header part -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td class="text-align:left">
                            <h2>PMD
                                <xsl:value-of select="//pmd/@version" /> Report
                            </h2>
                        </td>
                        <td class="text-align:center">
                            <h2>
                                <xsl:value-of select="$title" />
                            </h2>
                        </td>
                    </tr>
                    <tr>
                        <td class="text-align:left">
                            <small>
                                <i>Created:
                                    <xsl:call-template name="timestamp" />
                                </i>
                            </small>
                        </td>
                    </tr>
                </table>
                <hr size="1" />
                <!-- Summary part -->
                <h2>
                    <p>Section (i)</p>
                </h2>
                <xsl:apply-templates select="." mode="summary" />
                <hr size="1" width="100%" align="left" />
                <!-- Package List part -->
                <a name="top_section2"></a>
                <h2>
                    <p>Section (ii)</p>
                </h2>
                <xsl:apply-templates select="." mode="filelist" />
                <hr size="1" width="100%" align="left" />
                <!-- File part -->
                <a name="top_section3"></a>
                <h2>
                    <p>Section (iii)</p>
                </h2>
                <xsl:for-each select="file">
                    <xsl:sort select="@name" />
                    <xsl:apply-templates select="." />
                    <p />
                    <p />
                </xsl:for-each>
                <hr size="1" width="100%" align="left" />
                <!-- End-of-Report part -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td>
                            <b>End of
                                <a href="http://pmd.sourceforge.net">PMD
                                    <xsl:value-of select="//pmd/@version" />
                                </a>
                                report.
                            </b>
                        </td>
                    </tr>
                </table>
                <a href="#top">Back to top</a>
                <hr />
            </body>
        </html>
    </xsl:template>

    <xsl:template match="pmd" mode="summary">
        <h3>Summary</h3>
        <xsl:variable name="fileCount" select="count(file)" />
        <xsl:variable name="violationCount" select="count(file/violation)" />
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th>Files</th>
                <th width="14%">Violations</th>
                <th width="14%">Priority 1 (error -> high)</th>
                <th width="14%">Priority 2 (error -> normal)</th>
                <th width="14%">Priority 3 (warn -> high)</th>
                <th width="14%">Priority 4 (warn -> normal)</th>
                <th width="14%">Priority 5 (info)</th>
            </tr>
            <tr>
                <xsl:call-template name="alternated-row" />
                <td>
                    <xsl:value-of select="$fileCount" />
                </td>
                <td>
                    <xsl:value-of select="$violationCount" />
                </td>
                <td>
                    <div class="p1">
                        <xsl:value-of select="count(//violation[@priority = 1])" />
                    </div>
                </td>
                <td>
                    <div class="p2">
                        <xsl:value-of select="count(//violation[@priority = 2])" />
                    </div>
                </td>
                <td>
                    <div class="p3">
                        <xsl:value-of select="count(//violation[@priority = 3])" />
                    </div>
                </td>
                <td>
                    <div class="p4">
                        <xsl:value-of select="count(//violation[@priority = 4])" />
                    </div>
                </td>
                <td>
                    <div class="p5">
                        <xsl:value-of select="count(//violation[@priority = 5])" />
                    </div>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="pmd" mode="filelist">
        <h3>Files (Only highest priority violations per each file is highlighted)</h3>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th width="7%">Violations</th>
                <th>Name</th>
            </tr>
            <xsl:for-each select="file">
                <!-- Sort by weighted Priority (>9999 violations per priority will sort wrongly -->
                <xsl:sort data-type="string" order="descending"
                          select="count(violation[@priority = 1]) * 10000000000000000
              + count(violation[@priority = 2]) * 1000000000000
              + count(violation[@priority = 3]) * 100000000
              + count(violation[@priority = 4]) * 10000
              + count(violation[@priority = 5])" />
                <!-- <xsl:sort select="@name"/> -->
                <xsl:variable name="violationCount" select="count(violation)" />
                <xsl:variable name="myfname">
                    <xsl:choose>
                        <xsl:when test="string-length(substring-after(@name,$filepathheader)) &gt; 0">
                            <xsl:value-of select="substring-after(@name,$filepathheader)" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@name" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <!-- highlight with color of highest priority -->
                <xsl:variable name="highestPriorityColor">
                    <xsl:choose>
                        <xsl:when test="count(violation[@priority = 1]) &gt; 0">p1</xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="count(violation[@priority = 2]) &gt; 0">p2</xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                        <xsl:when test="count(violation[@priority = 3]) &gt; 0">p3</xsl:when>
                                        <xsl:otherwise>
                                            <xsl:choose>
                                                <xsl:when test="count(violation[@priority = 4]) &gt; 0">p4</xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:choose>
                                                        <xsl:when test="count(violation[@priority = 5]) &gt; 0">p5</xsl:when>
                                                    </xsl:choose>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="highestPriorityCount">
                    <xsl:choose>
                        <xsl:when test="count(violation[@priority = 1]) &gt; 0">
                            <xsl:value-of select="count(violation[@priority = 1])" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="count(violation[@priority = 2]) &gt; 0">
                                    <xsl:value-of select="count(violation[@priority = 2])" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                        <xsl:when test="count(violation[@priority = 3]) &gt; 0">
                                            <xsl:value-of select="count(violation[@priority = 3])" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:choose>
                                                <xsl:when test="count(violation[@priority = 4]) &gt; 0">
                                                    <xsl:value-of select="count(violation[@priority = 4])" />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:choose>
                                                        <xsl:when test="count(violation[@priority = 5]) &gt; 0">
                                                            <xsl:value-of select="count(violation[@priority = 5])" />
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <tr>
                    <xsl:call-template name="alternated-row" />
                    <td>
                        <div>
                            <xsl:attribute name="class">
                                <xsl:value-of select="$highestPriorityColor" />
                            </xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="$highestPriorityCount = $violationCount">
                                    <xsl:value-of select="$violationCount" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$highestPriorityCount" /> of
                                    <xsl:value-of select="$violationCount" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </td>
                    <td>
                        <a href="#f-{$myfname}">
                            <xsl:value-of select="$myfname" />
                        </a>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
        <a href="#top_section2">Back to top of section (ii)</a>
    </xsl:template>

    <xsl:template match="file">
        <xsl:variable name="myfname">
            <xsl:choose>
                <xsl:when test="string-length(substring-after(@name,$filepathheader)) &gt; 0">
                    <xsl:value-of select="substring-after(@name,$filepathheader)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@name" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <a name="f-{$myfname}"></a>
        <h3>File:
            <xsl:value-of select="$myfname" />
        </h3>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th width="7%">Priority</th>
                <th width="7%">Line(s)</th>
                <th width="15%">Method</th>
                <th>Violation Description</th>
                <th width="15%">Rule</th>
            </tr>
            <xsl:for-each select="violation">
                <xsl:sort data-type="number" order="ascending" select="@priority" />
                <tr>
                    <xsl:call-template name="alternated-row" />
                    <td>
                        <div>
                            <xsl:attribute name="class">
                                <xsl:call-template name="priorityDiv" />
                            </xsl:attribute>
                            <xsl:value-of disable-output-escaping="yes" select="@priority" />
                        </div>
                    </td>
                    <td>
                        <xsl:choose>
                            <xsl:when test="@beginline = @endline">
                                <xsl:value-of select="@beginline" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="@beginline" /> to
                                <xsl:value-of select="@endline" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                    <td>
                        <xsl:value-of select="@method" />
                    </td>
                    <td>
                        <xsl:if test="@externalInfoUrl">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="@externalInfoUrl" />
                                </xsl:attribute>
                                <xsl:call-template name="message" />
                            </a>
                        </xsl:if>
                        <xsl:if test="not(@externalInfoUrl)">
                            <xsl:call-template name="message" />
                        </xsl:if>
                    </td>
                    <td>
                        <xsl:value-of select="@rule" />
                    </td>
                </tr>
            </xsl:for-each>
        </table>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th width="100%">Total number of violations for this class:
                    <xsl:value-of select="count(violation)" />
                    <small> (P1=<xsl:value-of select="count(violation[@priority = 1])" />, 
                        P2=<xsl:value-of select="count(violation[@priority = 2])" />, 
                        P3=<xsl:value-of select="count(violation[@priority = 3])" />, 
                        P4=<xsl:value-of select="count(violation[@priority = 4])" />, 
                        P5=<xsl:value-of select="count(violation[@priority = 5])" />)
                    </small>
                </th>
            </tr>
        </table>
        <a href="#top_section3">Back to top of section (iii)</a>
    </xsl:template>

    <xsl:template name="alternated-row">
        <xsl:attribute name="class">
            <xsl:if test="position() mod 2 = 1">a</xsl:if>
            <xsl:if test="position() mod 2 = 0">b</xsl:if>
        </xsl:attribute>
    </xsl:template>

</xsl:stylesheet>

