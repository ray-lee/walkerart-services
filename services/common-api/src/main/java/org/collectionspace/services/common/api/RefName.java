package org.collectionspace.services.common.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class RefName {
    public static final String HACK_VOCABULARIES = "Vocabularies"; //TODO: get rid of these.
    public static final String HACK_ORGANIZATIONS = "Organizations"; //TODO: get rid of these.
    public static final String HACK_ORGAUTHORITIES = "Orgauthorities";  //TODO: get rid of these.
    public static final String HACK_PERSONAUTHORITIES = "Personauthorities";  //TODO: get rid of these.
    public static final String HACK_LOCATIONAUTHORITIES = "Locationauthorities";  //TODO: get rid of these.

    public static final String URN_PREFIX      = "urn:cspace:";
    public static final String URN_NAME_PREFIX = "urn:cspace:name";

    public static final String REFNAME = "refName";

    public static final String AUTHORITY_REGEX      = "urn:cspace:(.*):(.*)\\((.*)\\)\\'?([^\\']*)\\'?";
    public static final String AUTHORITY_ITEM_REGEX = "urn:cspace:(.*):(.*)\\((.*)\\):items\\((.*)\\)\\'?([^\\']*)\\'?";

    public static final String AUTHORITY_EXAMPLE  = "urn:cspace:collectionspace.org:Loansin(shortID)'displayName'";
    public static final String AUTHORITY_EXAMPLE2 = "urn:cspace:collectionspace.org:Loansin(shortID)";

    public static final String AUTHORITY_ITEM_EXAMPLE ="urn:cspace:collectionspace.org:Loansin(shortID):items(itemShortID)'itemDisplayName'";

    public static final String EX_tenantName = "collectionspace.org";
    public static final String EX_resource = "Loansin";
    public static final String EX_shortIdentifier = "shortID";
    public static final String EX_displayName = "displayName";
    public static final String EX_itemShortIdentifier = "itemShortID";
    public static final String EX_itemDisplayName = "itemDisplayName";


    public static class Authority {
        public String tenantName = "";
        public String resource = "";
        public String shortIdentifier = "";
        public String displayName = "";
        public static Authority parse(String urn) {
            Authority info = new Authority();
            Pattern p = Pattern.compile(AUTHORITY_REGEX);
            Matcher m = p.matcher(urn);
            if (m.find()){
                if (m.groupCount()<4){
                    return null;
                }
                info.tenantName = m.group(1);
                info.resource = m.group(2);
                info.shortIdentifier = m.group(3);
                info.displayName = m.group(4);
                return info;
            }
            return null;
        }
        public boolean equals(Object other){
            if (other == null){
                return false;
            }
            if (other instanceof Authority){
                Authority ao = (Authority)other;
                return (   this.tenantName.equals(ao.tenantName)
                        && this.resource.equals(ao.resource)
                        && this.shortIdentifier.equals(ao.shortIdentifier)
                       );
            } else {
                return false;
            }
        }
        public String getRelativeUri() {
        	return "/"+resource+"/"+URN_NAME_PREFIX+"("+shortIdentifier+")";
        }
        public String toString() {
            String displaySuffix = (displayName != null && (!displayName.isEmpty())) ? '\'' + displayName + '\'' : "";
            return URN_PREFIX + tenantName + ':' + resource + "(" + shortIdentifier + ")" + displaySuffix;
        }

    }

    public static class AuthorityItem {
        public Authority inAuthority;
        public String shortIdentifier = "";
        public String displayName = "";
        public static AuthorityItem parse(String urn) {
            Authority info = new Authority();
            AuthorityItem termInfo = new AuthorityItem();
            termInfo.inAuthority = info;
            Pattern p = Pattern.compile(AUTHORITY_ITEM_REGEX);
            Matcher m = p.matcher(urn);
            if (m.find()){
                if (m.groupCount()<5){
                    return  null;
                }
                termInfo.inAuthority.tenantName = m.group(1);
                termInfo.inAuthority.resource = m.group(2);
                termInfo.inAuthority.shortIdentifier = m.group(3);
                termInfo.shortIdentifier = m.group(4);
                termInfo.displayName = m.group(5);
                return termInfo;
            }
            return null;
        }
        public boolean equals(Object other){
            if (other == null){
                return false;
            }
            if (other instanceof AuthorityItem){
                AuthorityItem aio = (AuthorityItem)other;
                boolean ok = true;
                ok = ok && aio.inAuthority != null;
                ok = ok && aio.inAuthority.equals(this.inAuthority);
                ok = ok && aio.shortIdentifier.equals(this.shortIdentifier);
                ok = ok && aio.displayName.equals(this.displayName);
                return ok;
            } else {
                return false;
            }
        }
        public String getRelativeUri() {
        	return inAuthority.getRelativeUri()+"/items/"+URN_NAME_PREFIX+"("+shortIdentifier+")";
        }
        public String toString() {
            String displaySuffix = (displayName != null && (!displayName.isEmpty())) ? '\'' + displayName + '\'' : "";
            Authority ai = inAuthority;
            if (ai==null){
               return URN_PREFIX+"ERROR:inAuthorityNotSet: (" + shortIdentifier + ")" + displaySuffix;
            } else {
               String base = URN_PREFIX + ai.tenantName + ':' + ai.resource + "(" + ai.shortIdentifier + ")" ;
               String refname = base+":items("+shortIdentifier+")"+displaySuffix;
               return refname;
            }
        }

    }

    public static Authority buildAuthority(String tenantName, String serviceName, String authorityShortIdentifier, String authorityDisplayName) {
        Authority authority = new Authority();
        authority.tenantName = tenantName;
        authority.resource = serviceName;
        authority.shortIdentifier = authorityShortIdentifier;
        authority.displayName = authorityDisplayName;
        return authority;
    }

    public static AuthorityItem buildAuthorityItem(String tenantName, String serviceName, String authorityShortIdentifier,
                                                   String itemShortIdentifier, String itemDisplayName) {
        Authority authority = buildAuthority(tenantName, serviceName, authorityShortIdentifier, "");
        return buildAuthorityItem(authority, itemShortIdentifier, itemDisplayName);
    }

    public static AuthorityItem buildAuthorityItem(String authorityRefName, String itemShortID, String itemDisplayName) {
        Authority authority = Authority.parse(authorityRefName);
        AuthorityItem item = buildAuthorityItem(authority, itemShortID, itemDisplayName);
        return item;
    }

    public static AuthorityItem buildAuthorityItem(Authority authority, String itemShortIdentifier, String itemDisplayName) {
        AuthorityItem item = new AuthorityItem();
        item.inAuthority = authority;
        item.shortIdentifier = itemShortIdentifier;
        item.displayName = itemDisplayName;
        return item;
    }

    /** Use this method to avoid formatting any urn's outside of this unit;
     * Caller passes in a shortId, such as "TestAuthority", and method returns
     * the correct urn path element, without any path delimiters such as '/'
     * so that calling shortIdToPath("TestAuthority") returns "urn:cspace:name(TestAuthority)", and
     * then this value may be put into a path, such as "/personauthorities/urn:cspace:name(TestAuthority)/items".
     */
    public static String shortIdToPath(String shortId){
        return URN_NAME_PREFIX+'('+shortId+')';
    }
}
