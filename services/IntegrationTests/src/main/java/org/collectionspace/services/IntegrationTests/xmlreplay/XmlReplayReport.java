package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.collectionspace.services.common.XmlTools;
import org.collectionspace.services.common.api.Tools;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import java.util.ArrayList;
import java.util.List;

/**  Format a report based on XmlReplay ServiceResult object from a test group.
 * @author  laramie
 */
public class XmlReplayReport {
    public static final String HTML_PAGE_START = "<html><head><script type='text/javascript' src='reports-include.js'></script>"
                                                                         +"<LINK rel='stylesheet' href='reports-include.css'></LINK></head><body>";
    protected static final String HTML_PAGE_END = "</body></html>";
    protected static final String TOPLINKS = "<a class='TOPLINKS' href='javascript:openAll();'>Show All Payloads</a>" + "<a class='TOPLINKS' href='javascript:closeAll();'>Hide All Payloads</a>";

    protected static final String HTML_TEST_START = "<div class='TESTCASE'>";
    protected static final String HTML_TEST_END = "</div>";

    protected static final String GROUP_START = "<div class='TESTGROUP'>";
    protected static final String GROUP_END = "</div>";

    protected static final String RUNINFO_START = "<div class='RUNINFO'>";
    protected static final String RUNINFO_END = "</div>";


    protected static final String DIV_END = "</div>";

    protected static final String PRE_START = "<pre class='SUMMARY'>";
    protected static final String PRE_END = "</pre>";
    protected static final String BR = "<br />\r\n";

    protected static final String DETAIL_START = "<table border='1' class='DETAIL_TABLE'><tr><td>\r\n";
    protected static final String DETAIL_LINESEP = "</td></tr>\r\n<tr><td>";
    protected static final String DETAIL_END = "</td></tr></table>";

    private static final String SP = "&nbsp;&nbsp;&nbsp;";


    protected static String formatCollapse(String myDivID, String linkText){
        return  "<a href='javascript:;' onmousedown=\"toggleDiv('"+myDivID+"');\">"+linkText+"</a>"
                 + BR
                 + "<div ID='"+myDivID+"' class='PAYLOAD' style='display:none'>";
    }


    private StringBuffer header = new StringBuffer();
    private StringBuffer buffer = new StringBuffer();
    private String runInfo = "";
    //private StringBuffer toc = new StringBuffer();

    public String getPage(){
        return    HTML_PAGE_START
                    +"<div class='REPORTTIME'>XmlReplay run  "+Tools.nowLocale()+"</div>"
                    +header.toString()
                    +this.runInfo
                    +BR
                    +getTOC("").toString()
                    +BR
                    +buffer.toString()
                    +HTML_PAGE_END;
    }

    public String getTOC(String reportName){
        StringBuffer tocBuffer = new StringBuffer();

        if (Tools.notBlank(reportName)){
            // We are generating an index.html file.
            tocBuffer.append(this.header.toString());
        }
        tocBuffer.append(BR).append(TOPLINKS).append(BR);
        for (TOC toc: tocList){
              tocBuffer.append(BR+"<a href='"+reportName+"#TOC"+toc.tocID+"'>"+toc.testID+"</a> "+ toc.detail);
        }
        tocBuffer.append(BR+BR);
        return tocBuffer.toString();
    }

    public void addRunInfo(String text){
         this.runInfo = RUNINFO_START+text+RUNINFO_END;
         //addText(this.runInfo);
    }


    public void addText(String text){
         buffer.append(text);
    }
    public void addTestGroup(String groupID, String controlFile){
        header.append(GROUP_START);
        header.append(lbl("Test Group")).append(groupID).append(SP).append(lbl("Control File")).append(controlFile);
        header.append(GROUP_END);
    }

    private int divID = 0;

    public void addTestResult(ServiceResult serviceResult){
        buffer.append(HTML_TEST_START);
        int tocID = ++divID;
        buffer.append(formatSummary(serviceResult, tocID));
        buffer.append(formatPayloads(serviceResult, tocID));
        buffer.append(HTML_TEST_END);
    }

    public static class TOC {
        public int tocID;
        public String testID;
        public String detail;
    }
    private List<TOC> tocList = new ArrayList<TOC>();

    protected String formatSummary(ServiceResult serviceResult, int tocID){
        TOC toc = new TOC();
        toc.tocID = tocID;
        toc.testID = serviceResult.testID;
        toc.detail =  (serviceResult.gotExpectedResult() ? ok("SUCCESS") : red("FAILURE") );
        tocList.add(toc);

        StringBuffer fb = new StringBuffer();
        fb.append("<a name='TOC"+tocID+"'></a>");
        fb.append(detail(serviceResult, false, false, DETAIL_START, DETAIL_LINESEP, DETAIL_END, tocID));
        return fb.toString();
    }

    protected String formatPayloads(ServiceResult serviceResult, int tocID){
        StringBuffer fb = new StringBuffer();
        fb.append(BR);
        appendPayload(fb, serviceResult.requestPayload, "REQUEST", "REQUEST" + tocID);
        appendPayload(fb, serviceResult.requestPayloadsRaw, "REQUEST (RAW)", "REQUESTRAW" + tocID);
        appendPayload(fb, serviceResult.result, "RESPONSE", "RESPONSE" + tocID);
        fb.append(BR);

        return fb.toString();
    }

    protected void appendPayload( StringBuffer fb , String payload, String title, String theDivID){
        if (Tools.notBlank(payload)){
            //fb.append(BR+title+":"+BR);
            try {
                String pretty = prettyPrint(payload);


                fb.append(formatCollapse(theDivID, title));  //starts a div.
                fb.append(PRE_START);
                fb.append(escape(pretty));
                fb.append(PRE_END);
                fb.append(DIV_END);                        //ends that div.
            } catch (Exception e){
                String error = "<font color='red'>ERROR:</font> "+payload;
                fb.append(error);
                fb.append(BR).append(BR);
            }
        }
    }

    private String escape(String source){
        try {
            return Tools.searchAndReplace(source, "<", "&lt;");
        } catch (Exception e){
            return "ERROR escaping requestPayload"+e;
        }
    }

    private String prettyPrint(String rawXml) throws Exception {
        Document document = DocumentHelper.parseText(rawXml);
        return XmlTools.prettyPrint(document, "    ");
    }

    private static final String LINE = "<hr />\r\n";
    private static final String CRLF = "<br />\r\n";

    protected String red(String label){
        return "<span class='ERROR'>"+label+"</span> ";
    }
    protected String ok(String label){
        return "<span class='OK'>"+label+"</span> ";
    }
    protected String lbl(String label){
        return "<span class='LABEL'>"+label+":</span> ";
    }
    public String detail(ServiceResult s, boolean includePayloads, boolean includePartSummary, String start, String linesep, String end, int tocID){
        String partSummary = s.partsSummary(includePartSummary);
        String res =  start
                + ( s.gotExpectedResult() ? lbl("SUCCESS") : "<font color='red'><b>FAILURE</b></font>"  )
                  + SP + ( Tools.notEmpty(s.testID) ? s.testID : "" )
                  + SP +linesep
                +s.method+ SP +"<a href='"+s.fullURL+"'>"+s.fullURL+"</a>"                                     +linesep
                + s.responseCode+ SP +lbl("gotExpected")+s.gotExpectedResult()                                                       +linesep
                + (Tools.notBlank(s.failureReason) ? s.failureReason +linesep : "" )
                + ( (s.expectedCodes.size()>0) ? lbl("expectedCodes")+s.expectedCodes+linesep : "" )
                //+ ( Tools.notEmpty(s.testGroupID) ? "testGroupID:"+s.testGroupID+linesep : "" )
                //THIS WORKS, BUT IS VERBOSE: + ( Tools.notEmpty(s.fromTestID) ? "fromTestID:"+s.fromTestID+linesep : "" )
                + ( Tools.notEmpty(s.responseMessage) ? lbl("msg")+s.responseMessage+linesep : "" )
                +lbl("auth")+s.auth                                                                                        +linesep
                + ( Tools.notEmpty(s.deleteURL) ? lbl("deleteURL")+s.deleteURL+linesep : "" )
                + ( Tools.notEmpty(s.location) ? lbl("location.CSID")+s.location+linesep : "" )
                + ( Tools.notEmpty(s.error) ? "ERROR:"+s.error +linesep : "" )
                + ((includePartSummary && Tools.notBlank(partSummary)) ? lbl("part summary")+partSummary +linesep : "")
                + ( includePayloads && Tools.notBlank(s.requestPayload) ? LINE+lbl("requestPayload")+LINE+CRLF+s.requestPayload+LINE : "" )
                + ( includePayloads && Tools.notBlank(s.result) ? LINE+lbl("result")+LINE+CRLF+s.result : "" )
                +end;
        return res;
    }

}
