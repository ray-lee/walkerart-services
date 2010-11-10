package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.apache.commons.cli.*;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

/**  This class is used to replay a request to the Services layer, by sending the XML payload
 *   in an appropriate Multipart request.
 *   See example usage in calling class XmlReplayTest in services/IntegrationTests, and also in main() in this class.
 *   @author Laramie Crocker
 */
public class XmlReplay {

    public XmlReplay(String basedir){
        this.basedir = basedir;
        this.serviceResultsMap = createResultsMap();
    }

    public static final String DEFAULT_CONTROL = "xml-replay-control.xml";
    public static final String DEFAULT_MASTER_CONTROL = "xml-replay-master.xml";

    private String basedir = ".";  //set from constructor.
    public String getBaseDir(){
        return basedir;
    }
    
    private String controlFileName = DEFAULT_CONTROL;
    public String getControlFileName() {
        return controlFileName;
    }
    public void setControlFileName(String controlFileName) {
        this.controlFileName = controlFileName;
    }

    private String protoHostPort = "";
    public String getProtoHostPort() {
        return protoHostPort;
    }
    public void setProtoHostPort(String protoHostPort) {
        this.protoHostPort = protoHostPort;
    }

    private boolean autoDeletePOSTS = true;
    public boolean isAutoDeletePOSTS() {
        return autoDeletePOSTS;
    }
    public void setAutoDeletePOSTS(boolean autoDeletePOSTS) {
        this.autoDeletePOSTS = autoDeletePOSTS;
    }

    private Dump dump;
    public Dump getDump() {
        return dump;
    }
    public void setDump(Dump dump) {
        this.dump = dump;
    }

    AuthsMap defaultAuthsMap;
    public AuthsMap getDefaultAuthsMap(){
        return defaultAuthsMap;
    }
    public void setDefaultAuthsMap(AuthsMap authsMap){
        defaultAuthsMap = authsMap;
    }

    private Map<String, ServiceResult> serviceResultsMap;
    public Map<String, ServiceResult> getServiceResultsMap(){
        return serviceResultsMap;
    }
    public static Map<String, ServiceResult> createResultsMap(){
        return new HashMap<String, ServiceResult>();
    }


    public String toString(){
        return "XmlReplay{"+this.basedir+", "+this.controlFileName+", "+this.defaultAuthsMap+", "+this.dump+'}';
    }

    // ============== METHODS ===========================================================

    public Document openMasterConfigFile(String masterFilename) throws FileNotFoundException {
        Document document = getDocument(Tools.glue(basedir, "/", masterFilename)); //will check full path first, then checks relative to PWD.
        if (document == null){
            throw new FileNotFoundException("XmlReplay master control file ("+masterFilename+") not found in basedir: "+basedir+". Exiting test.");
        }
        return document;
    }

    /** specify the master config file, relative to getBaseDir(), but ignore any tests or testGroups in the master.
     *  @return a Document object, which you don't need to use: all options will be stored in XmlReplay instance.
     */
    public Document readOptionsFromMasterConfigFile(String masterFilename) throws FileNotFoundException {
        Document document = openMasterConfigFile(masterFilename);
        protoHostPort = document.selectSingleNode("/xmlReplayMaster/protoHostPort").getText().trim();
        AuthsMap authsMap = readAuths(document);
        setDefaultAuthsMap(authsMap);
        Dump dump = XmlReplay.readDumpOptions(document);
        setDump(dump);
        return document;
    }

    public List<List<ServiceResult>> runMaster(String masterFilename) throws Exception {
        return runMaster(masterFilename, true);
    }

    /** Creates new instances of XmlReplay, one for each controlFile specified in the master,
     *  and setting defaults from this instance, but not sharing ServiceResult objects or maps. */
    public List<List<ServiceResult>> runMaster(String masterFilename, boolean readOptionsFromMaster) throws Exception {
        List<List<ServiceResult>> list = new ArrayList<List<ServiceResult>>();
        Document document;
        if (readOptionsFromMaster){
            document = readOptionsFromMasterConfigFile(masterFilename);
        } else {
            document = openMasterConfigFile(masterFilename);
        }
        String controlFile, testGroup, test;
        List<Node> runNodes;
        runNodes = document.selectNodes("/xmlReplayMaster/run");
        for (Node runNode : runNodes) {
            controlFile = runNode.valueOf("@controlFile");
            testGroup = runNode.valueOf("@testGroup");
            test = runNode.valueOf("@test"); //may be empty

            //Create a new instance and clone only config values, not any results maps.
            XmlReplay replay = new XmlReplay(basedir);
            replay.setControlFileName(controlFile);
            replay.setProtoHostPort(protoHostPort);
            replay.setAutoDeletePOSTS(isAutoDeletePOSTS());
            replay.setDump(dump);
            replay.setDefaultAuthsMap(getDefaultAuthsMap());

            //Now run *that* instance.
            List<ServiceResult> results = replay.runTests(testGroup, test);
            list.add(results);
        }
        return list;
    }

    /** Use this if you wish to named tests within a testGroup, otherwise call runTestGroup(). */
    public List<ServiceResult>  runTests(String testGroupID, String testID) throws Exception {
        List<ServiceResult> result = runXmlReplayFile(this.basedir,
                                this.controlFileName,
                                testGroupID,
                                testID,
                                this.serviceResultsMap,
                                this.autoDeletePOSTS,
                                dump,
                                this.protoHostPort,
                                this.defaultAuthsMap);
        return result;
    }

    /** Use this if you wish to specify just ONE test to run within a testGroup, otherwise call runTestGroup(). */
    public ServiceResult  runTest(String testGroupID, String testID) throws Exception {
        List<ServiceResult> result = runXmlReplayFile(this.basedir,
                                this.controlFileName,
                                testGroupID,
                                testID,
                                this.serviceResultsMap,
                                this.autoDeletePOSTS,
                                dump,
                                this.protoHostPort,
                                this.defaultAuthsMap);
        if (result.size()>1){
            throw new IndexOutOfBoundsException("Multiple ("+result.size()+") tests with ID='"+testID+"' were found within test group '"+testGroupID+"', but there should only be one test per ID attribute.");
        }
        return result.get(0);
    }

    /** Use this if you wish to run all tests within a testGroup.*/
    public List<ServiceResult> runTestGroup(String testGroupID) throws Exception {
        //NOTE: calling runTest with empty testID runs all tests in a test group, but don't expose this fact.
        // Expose this method (runTestGroup) instead.
        return runTests(testGroupID, "");
    }

    public List<ServiceResult>  autoDelete(String logName){
        return autoDelete(this.serviceResultsMap, logName);
    }

    /** Use this method to clean up resources created on the server that returned CSIDs, if you have
     *  specified autoDeletePOSTS==false, which means you are managing the cleanup yourself.
     * @param serviceResultsMap a Map of ServiceResult objects, which will contain ServiceResult.deleteURL.
     * @return a List<String> of debug info about which URLs could not be deleted.
     */
    public static List<ServiceResult> autoDelete(Map<String, ServiceResult> serviceResultsMap, String logName){
        List<ServiceResult> results = new ArrayList<ServiceResult>();
        for (ServiceResult pr : serviceResultsMap.values()){
            try {
                ServiceResult deleteResult = XmlReplayTransport.doDELETE(pr.deleteURL, pr.auth, pr.testID, "[autodelete:"+logName+"]");
                results.add(deleteResult);
            } catch (Throwable t){
                String s = (pr!=null) ? "ERROR while cleaning up ServiceResult map: "+pr+" for "+pr.deleteURL+" :: "+t
                                      : "ERROR while cleaning up ServiceResult map (null ServiceResult): "+t;
                System.err.println(s);
                ServiceResult errorResult = new ServiceResult();
                errorResult.fullURL = pr.fullURL;
                errorResult.testGroupID = pr.testGroupID;
                errorResult.fromTestID = pr.fromTestID;
                errorResult.error = s;
                results.add(errorResult);
            }
        }
        return results;
    }

    public static class AuthsMap {
        Map<String,String> map;
        String defaultID="";
        public String getDefaultAuth(){
            return map.get(defaultID);
        }
        public String toString(){
            return "AuthsMap: {default='"+defaultID+"'; "+map.keySet()+'}';
        }
    }

    public static AuthsMap readAuths(Document document){
    Map<String, String> map = new HashMap<String, String>();
        List<Node> authNodes = document.selectNodes("//auths/auth");
        for (Node auth : authNodes) {
            map.put(auth.valueOf("@ID"), auth.getStringValue());
        }
        AuthsMap authsMap = new AuthsMap();
        Node auths = document.selectSingleNode("//auths");
        String defaultID = "";
        if (auths != null){
            defaultID = auths.valueOf("@default");
        }
        authsMap.map = map;
        authsMap.defaultID = defaultID;
        return authsMap;
    }

    public static class Dump {
        public boolean payloads = false;
        //public static final ServiceResult.DUMP_OPTIONS dumpServiceResultOptions = ServiceResult.DUMP_OPTIONS;
        public ServiceResult.DUMP_OPTIONS dumpServiceResult = ServiceResult.DUMP_OPTIONS.minimal;
        public String toString(){
            return "payloads: "+payloads+" dumpServiceResult: "+dumpServiceResult;
        }
    }

    public static Dump getDumpConfig(){
        return new Dump();
    }

    public static Dump readDumpOptions(Document document){
        Dump dump = getDumpConfig();
        Node dumpNode = document.selectSingleNode("//dump");
        if (dumpNode != null){
            dump.payloads = Tools.isTrue(dumpNode.valueOf("@payloads"));
            String dumpServiceResultStr = dumpNode.valueOf("@dumpServiceResult");
            if (Tools.notEmpty(dumpServiceResultStr)){
                dump.dumpServiceResult = ServiceResult.DUMP_OPTIONS.valueOf(dumpServiceResultStr);
            }
        }
        return dump;
    }

    private static class PartsStruct {
        public List<String> partsList = new ArrayList<String>();
        public List<String> filesList = new ArrayList<String>();
        boolean bDoingSinglePartPayload = false;
        String singlePartPayloadFilename = "";
        String overrideTestID = "";
        public static PartsStruct readParts(Node testNode, final String testID, String xmlReplayBaseDir){
            PartsStruct result = new PartsStruct();
            result.singlePartPayloadFilename = testNode.valueOf("filename");
            String singlePartPayloadFilename = testNode.valueOf("filename");
            if (Tools.notEmpty(singlePartPayloadFilename)){
                result.bDoingSinglePartPayload = true;
                result.singlePartPayloadFilename = xmlReplayBaseDir + '/' + singlePartPayloadFilename;
            } else {
                result.bDoingSinglePartPayload = false;
                List<Node> parts = testNode.selectNodes("parts/part");
                if (parts == null || parts.size()==0){  //path is just /testGroup/test/part/
                    String commonPartName = testNode.valueOf("part/label");
                    String testfile = testNode.valueOf("part/filename");
                    String fullTestFilename = xmlReplayBaseDir + '/' + testfile;
                    if ( Tools.isEmpty(testID) ){
                        result.overrideTestID = testfile; //It is legal to have a missing ID attribute, and rely on a unique filename.
                    }
                    result.partsList.add(commonPartName);
                    result.filesList.add(fullTestFilename);
                } else { // path is /testGroup/test/parts/part/
                    for (Node part : parts){
                        String commonPartName = part.valueOf("label");
                        String filename = part.valueOf("filename");
                        String fullTestFilename = xmlReplayBaseDir + '/' + filename;
                        if ( Tools.isEmpty(testID) ){  //if testID is empty, we'll use the *first*  filename as ID.
                            result.overrideTestID = filename; //It is legal to have a missing ID attribute, and rely on a unique filename.
                        }
                        result.partsList.add(commonPartName);
                        result.filesList.add(fullTestFilename);
                    }
                }
            }
            return result;
        }
    }

    private static String fixupFullURL(String fullURL, String protoHostPort, String uri){
        if ( ! uri.startsWith(protoHostPort)){
            fullURL = Tools.glue(protoHostPort, "/", uri);
        } else {
            fullURL = uri;
        }
        return fullURL;
    }

    private static String fromTestID(String fullURL, Node testNode, Map<String, ServiceResult> serviceResultsMap){
        String fromTestID = testNode.valueOf("fromTestID");
        if (Tools.notEmpty(fromTestID)){
            ServiceResult getPR = serviceResultsMap.get(fromTestID);
            if (getPR != null){
                fullURL = Tools.glue(fullURL, "/", getPR.location);
            }
        }
        return fullURL;
    }

    private static String CSIDfromTestID(Node testNode, Map<String, ServiceResult> serviceResultsMap){
        String result = "";
        String fromTestID = testNode.valueOf("fromTestID");
        if (Tools.notEmpty(fromTestID)){
            ServiceResult getPR = serviceResultsMap.get(fromTestID);
            if (getPR != null){
                result = getPR.location;
            }
        }
        return result;
    }


    public static org.dom4j.Document getDocument(String xmlFileName) {
        Document document = null;
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(xmlFileName);
        } catch (DocumentException e) {
            //e.printStackTrace();
        }
        return document;
    }


    //================= runXmlReplayFile ======================================================

    public static List<ServiceResult> runXmlReplayFile(String xmlReplayBaseDir,
                                          String controlFileName,
                                          String testGroupID,
                                          String oneTestID,
                                          Map<String, ServiceResult> serviceResultsMap,
                                          boolean param_autoDeletePOSTS,
                                          Dump dump,
                                          String protoHostPortParam,
                                          AuthsMap defaultAuths)
                                          throws Exception {
        //Internally, we maintain two collections of ServiceResult:
        //  the first is the return value of this method.
        //  the second is the serviceResultsMap, which is used for keeping track of CSIDs created by POSTs, for later reference by DELETE, etc.
        List<ServiceResult> results = new ArrayList<ServiceResult>();

        String controlFile = Tools.glue(xmlReplayBaseDir, "/", controlFileName);
        Document document;
        document = getDocument(controlFile); //will check full path first, then checks relative to PWD.
        if (document==null){
            throw new FileNotFoundException("XmlReplay control file ("+controlFileName+") not found in basedir: "+xmlReplayBaseDir+" Exiting test.");
        }
        String protoHostPort;
        if (Tools.isEmpty(protoHostPortParam)){
            protoHostPort = document.selectSingleNode("/xmlReplay/protoHostPort").getText().trim();
            System.out.println("DEPRECATED: Using protoHostPort ('"+protoHostPort+"') from xmlReplay file ('"+controlFile+"'), not master.");
        } else {
            protoHostPort = protoHostPortParam;
        }
        if (Tools.isEmpty(protoHostPort)){
            throw new Exception("XmlReplay control file must have a protoHostPort element");
        }

        String authsMapINFO;
        AuthsMap authsMap = readAuths(document);
        if (authsMap.map.size()==0){
            authsMap = defaultAuths;
            authsMapINFO = "Using defaultAuths from master file: "+defaultAuths;
        } else {
            authsMapINFO = "Using AuthsMap from control file: "+authsMap;
        }
        System.out.println("XmlReplay running:"
                          +"\r\n   controlFile: "+ (new File(controlFile).getCanonicalPath())
                          +"\r\n   protoHostPort: "+protoHostPort
                          +"\r\n   testGroup: "+testGroupID
                          + (Tools.notEmpty(oneTestID) ? "\r\n   oneTestID: "+oneTestID : "")
                          +"\r\n   AuthsMap: "+authsMapINFO
                          +"\r\n   param_autoDeletePOSTS: "+param_autoDeletePOSTS
                          +"\r\n   Dump info: "+dump
                          +"\r\n");

        String autoDeletePOSTS = "";
        List<Node> testgroupNodes;
        if (Tools.notEmpty(testGroupID)){
            testgroupNodes = document.selectNodes("//testGroup[@ID='"+testGroupID+"']");
        } else {
            testgroupNodes = document.selectNodes("//testGroup");
        }

        JexlEngine jexl = new JexlEngine();   // Used for expression language expansion from uri field.
        XmlReplayEval evalStruct = new XmlReplayEval();
        evalStruct.serviceResultsMap = serviceResultsMap;
        evalStruct.jexl = jexl;

        for (Node testgroup : testgroupNodes) {
            JexlContext jc = new MapContext();  //Get a new JexlContext for each test group.
            evalStruct.jc = jc;

            autoDeletePOSTS = testgroup.valueOf("@autoDeletePOSTS");
            List<Node> tests;
            if (Tools.notEmpty(oneTestID)){
                tests = testgroup.selectNodes("test[@ID='"+oneTestID+"']");
            } else {
                tests = testgroup.selectNodes("test");
            }
            String authForTest = "";
            int testElementIndex = -1;

            for (Node testNode : tests) {
                try {
                    testElementIndex++;
                    String testID = testNode.valueOf("@ID");
                    String testIDLabel = Tools.notEmpty(testID) ? (testGroupID+'.'+testID) : (testGroupID+'.'+testElementIndex);
                    String method = testNode.valueOf("method");
                    String uri = testNode.valueOf("uri");
                    String fullURL = Tools.glue(protoHostPort, "/", uri);
                    String initURI = uri;

                    String authIDForTest = testNode.valueOf("@auth");
                    String currentAuthForTest = authsMap.map.get(authIDForTest);
                    if (Tools.notEmpty(currentAuthForTest)){
                        authForTest = currentAuthForTest; //else just run with current from last loop;
                    }
                    if (Tools.isEmpty(authForTest)){
                        authForTest = defaultAuths.getDefaultAuth();
                    }

                    if (uri.indexOf("$")>-1){
                        uri = evalStruct.eval(uri, serviceResultsMap, jexl, jc);
                    }
                    fullURL = fixupFullURL(fullURL, protoHostPort, uri);

                    List<Integer> expectedCodes = new ArrayList<Integer>();
                    String expectedCodesStr = testNode.valueOf("expectedCodes");
                    if (Tools.notEmpty(expectedCodesStr)){
                         String[] codesArray = expectedCodesStr.split(",");
                         for (String code : codesArray){
                             expectedCodes.add(new Integer(code.trim()));
                         }
                    }

                    ServiceResult serviceResult;
                    boolean isPOST = method.equalsIgnoreCase("POST");
                    boolean isPUT =  method.equalsIgnoreCase("PUT");
                    if ( isPOST || isPUT ) {
                        PartsStruct parts = PartsStruct.readParts(testNode, testID, xmlReplayBaseDir);
                        if (Tools.notEmpty(parts.overrideTestID)) {
                            testID = parts.overrideTestID;
                        }
                        if (isPOST){
                            String csid = CSIDfromTestID(testNode, serviceResultsMap);
                            if (Tools.notEmpty(csid)) uri = Tools.glue(uri, "/", csid+"/items/");
                        } else if (isPUT) {
                            uri = fromTestID(uri, testNode, serviceResultsMap);
                        }
                        if (parts.bDoingSinglePartPayload){
                            serviceResult = XmlReplayTransport.doPOST_PUTFromXML(parts.singlePartPayloadFilename, protoHostPort, uri, "POST", XmlReplayTransport.APPLICATION_XML, evalStruct, authForTest, testIDLabel);
                        } else {
                            serviceResult = XmlReplayTransport.doPOST_PUTFromXML_Multipart(parts.filesList, parts.partsList, protoHostPort, uri, "POST", evalStruct, authForTest, testIDLabel);
                        }
                        results.add(serviceResult);
                        if (isPOST){
                            serviceResultsMap.put(testID, serviceResult);      //PUTs do not return a Location, so don't add PUTs to serviceResultsMap.
                        }
                        fullURL = fixupFullURL(fullURL, protoHostPort, uri);
                    } else if (method.equalsIgnoreCase("DELETE")){
                        String fromTestID = testNode.valueOf("fromTestID");
                        ServiceResult pr = serviceResultsMap.get(fromTestID);
                        if (pr!=null){
                            serviceResult = XmlReplayTransport.doDELETE(pr.deleteURL, authForTest, testIDLabel, fromTestID);
                            serviceResult.fromTestID = fromTestID;
                            results.add(serviceResult);
                            if (serviceResult.gotExpectedResult()){
                                serviceResultsMap.remove(fromTestID);
                            }
                        } else {
                            if (Tools.notEmpty(fromTestID)){
                                serviceResult = new ServiceResult();
                                serviceResult.responseCode = 0;
                                serviceResult.error = "ID not found in element fromTestID: "+fromTestID;
                                System.err.println("****\r\nServiceResult: "+serviceResult.error+". SKIPPING TEST. Full URL: "+fullURL);
                            } else {
                                serviceResult = XmlReplayTransport.doDELETE(fullURL, authForTest, testID, fromTestID);
                            }
                            serviceResult.fromTestID = fromTestID;
                            results.add(serviceResult);
                        }
                    } else if (method.equalsIgnoreCase("GET")){
                        fullURL = fromTestID(fullURL, testNode, serviceResultsMap);
                        serviceResult = XmlReplayTransport.doGET(fullURL, authForTest, testIDLabel);
                        results.add(serviceResult);
                    } else if (method.equalsIgnoreCase("LIST")){
                        fullURL = fixupFullURL(fullURL, protoHostPort, uri);
                        String listQueryParams = ""; //TODO: empty for now, later may pick up from XML control file.
                        serviceResult = XmlReplayTransport.doLIST(fullURL, listQueryParams, authForTest, testIDLabel);
                        results.add(serviceResult);
                    } else {
                        throw new Exception("HTTP method not supported by XmlReplay: "+method);
                    }

                    serviceResult.testID = testID;
                    serviceResult.fullURL = fullURL;
                    serviceResult.auth = authForTest;
                    serviceResult.method = method;
                    if (expectedCodes.size()>0){
                        serviceResult.expectedCodes = expectedCodes;
                    }
                    if (Tools.isEmpty(serviceResult.testID)) serviceResult.testID = testIDLabel;
                    if (Tools.isEmpty(serviceResult.testGroupID)) serviceResult.testGroupID = testGroupID;

                    String serviceResultRow = serviceResult.dump(dump.dumpServiceResult);
                    String leader = (dump.dumpServiceResult == ServiceResult.DUMP_OPTIONS.detailed) ? "XmlReplay:"+testIDLabel+": ": "";
                    System.out.println(leader+serviceResultRow+"\r\n");
                    if (dump.payloads) System.out.println(serviceResult.result);
                } catch (Throwable t) {
                    System.out.println("ERROR: XmlReplay experienced an error in a test node: "+testNode+" Throwable: "+t);
                }
            }
            if (Tools.isTrue(autoDeletePOSTS)&&param_autoDeletePOSTS){
                autoDelete(serviceResultsMap, "default");
            }
        }
        return results;
    }


    //======================== MAIN ===================================================================

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("xmlReplayBaseDir", true, "default/basedir");
        return options;
    }

    public static String usage(){
        String result = "org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay {args}\r\n"
                        +"  -xmlReplayBaseDir <dir> \r\n"
                        +" You may also override these with system args, e.g.: \r\n"
                        +"   -DxmlReplayBaseDir=/path/to/dir \r\n"
                        +" These may also be passed in via the POM.\r\n"
                        +" You can also set these system args, e.g.: \r\n"
                        +"  -DtestGroupID=<oneID> \r\n"
                        +"  -DtestID=<one TestGroup ID>"
                        +"  -DautoDeletePOSTS=<true|false> \r\n"
                        +"    (note: -DautoDeletePOSTS won't force deletion if set to false in control file.";
        return result;
    }

    private static String opt(CommandLine line, String option){
        String result;
        String fromProps = System.getProperty(option);
        if (Tools.notEmpty(fromProps)){
            return fromProps;
        }
        result = line.getOptionValue(option);
        if (result == null){
            result = "";
        }
        return result;
    }

    public static void main(String[]args) throws Exception {
        Options options = createOptions();
        //System.out.println("System CLASSPATH: "+prop.getProperty("java.class.path", null));
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            String xmlReplayBaseDir = opt(line, "xmlReplayBaseDir");
            String testGroupID      = opt(line, "testGroupID");
            String testID           = opt(line, "testID");
            String autoDeletePOSTS  = opt(line, "autoDeletePOSTS");
            String dumpResults      = opt(line, "dumpResults");
            String controlFilename   = opt(line, "controlFilename");
            String xmlReplayMaster  = opt(line, "xmlReplayMaster");

            xmlReplayBaseDir = Tools.fixFilename(xmlReplayBaseDir);
            controlFilename = Tools.fixFilename(controlFilename);

            boolean bAutoDeletePOSTS = true;
            if (Tools.notEmpty(autoDeletePOSTS)) {
                bAutoDeletePOSTS = Tools.isTrue(autoDeletePOSTS);
            }
            boolean bDumpResults = false;
            if (Tools.notEmpty(dumpResults)) {
                bDumpResults = Tools.isTrue(autoDeletePOSTS);
            }
            if (Tools.isEmpty(xmlReplayBaseDir)){
                System.err.println("ERROR: xmlReplayBaseDir was not specified.");
                return;
            }
            File f = new File(Tools.glue(xmlReplayBaseDir, "/", controlFilename));
            if (Tools.isEmpty(xmlReplayMaster) && !f.exists()){
                System.err.println("Control file not found: "+f.getCanonicalPath());
                return;
            }
            File fMaster = new File(Tools.glue(xmlReplayBaseDir, "/", xmlReplayMaster));
            if (Tools.notEmpty(xmlReplayMaster)  && !fMaster.exists()){
                System.err.println("Master file not found: "+fMaster.getCanonicalPath());
                return;
            }

            String xmlReplayBaseDirResolved = (new File(xmlReplayBaseDir)).getCanonicalPath();
            System.out.println("XmlReplay ::"
                            + "\r\n    xmlReplayBaseDir: "+xmlReplayBaseDir
                            + "\r\n    xmlReplayBaseDir(resolved): "+xmlReplayBaseDirResolved
                            + "\r\n    controlFilename: "+controlFilename
                            + "\r\n    xmlReplayMaster: "+xmlReplayMaster
                            + "\r\n    testGroupID: "+testGroupID
                            + "\r\n    testID: "+testID
                            + "\r\n    autoDeletePOSTS: "+bAutoDeletePOSTS
                            + (Tools.notEmpty(xmlReplayMaster)
                                       ? ("\r\n    will use master file: "+fMaster.getCanonicalPath())
                                       : ("\r\n    will use control file: "+f.getCanonicalPath()) )
                             );
            
            if (Tools.notEmpty(xmlReplayMaster)){
                if (Tools.notEmpty(controlFilename)){
                    System.out.println("WARN: controlFilename: "+controlFilename+" will not be used because master was specified.  Running master: "+xmlReplayMaster);
                }
                XmlReplay replay = new XmlReplay(xmlReplayBaseDirResolved);
                replay.readOptionsFromMasterConfigFile(xmlReplayMaster);
                replay.setAutoDeletePOSTS(bAutoDeletePOSTS);
                Dump dumpFromMaster = replay.getDump();
                dumpFromMaster.payloads = Tools.isTrue(dumpResults);
                replay.setDump(dumpFromMaster);
                replay.runMaster(xmlReplayMaster, false); //false, because we already just read the options, and override a few.
            } else {
                Dump dump = getDumpConfig();
                dump.payloads = Tools.isTrue(dumpResults);
                runXmlReplayFile(xmlReplayBaseDirResolved, controlFilename, testGroupID, testID, createResultsMap(), bAutoDeletePOSTS, dump, "", null);
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Cmd-line parsing failed.  Reason: " + exp.getMessage());
            System.err.println(usage());
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
