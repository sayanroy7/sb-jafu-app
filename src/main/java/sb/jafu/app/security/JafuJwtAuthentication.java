package sb.jafu.app.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

public class JafuJwtAuthentication implements Authentication {
	private static final long serialVersionUID = 5747420049243716847L;

	private final String idToken;
	private final Claims accessTokenClaims;
	private final Claims idTokenClaims;

	private boolean authenticated = false;
	private final Collection<GrantedAuthority> authorities;
	private final List<String> customers;
	private final List<String> scopes;

	public enum AuthenticationType {
		APIKEY, OAUTH2, UNKNOWN;

		public static AuthenticationType fromValue(String value){
			for(AuthenticationType type: AuthenticationType.values()){
				if(type.toString().equalsIgnoreCase(value)){
					return type;
				}
			}
			return UNKNOWN;
		}
	}

	/**
	 * Created by the <tt>XCJWTAuthenticationProvider</tt> on successful authentication.
	 */
	public JafuJwtAuthentication(String idToken, Claims idTokenClaims, Claims accessTokenClaims) {
		this.idToken = idToken;
		this.idTokenClaims = idTokenClaims;
		this.accessTokenClaims = accessTokenClaims;
		setAuthenticated(true);
		// parse scopes
		scopes = (List<String>)accessTokenClaims.get("scope");
        if (scopes == null || scopes.isEmpty()) {
        	this.authorities = AuthorityUtils.NO_AUTHORITIES;
        } else {
            List<GrantedAuthority> tempauthorities = scopes.stream().map(scope -> new SimpleGrantedAuthority(scope)).collect(Collectors.toList());
            this.authorities = Collections.unmodifiableList(tempauthorities);
        }
        // parse tenants
        List<String> tempcustomers = (List<String>)idTokenClaims.get("tenants");
        if (tempcustomers == null || tempcustomers.isEmpty()) {
        	this.customers = new ArrayList<>();
        } else {
        	this.customers = Collections.unmodifiableList(tempcustomers);
        }
	}
	
	@Override
	public String getName() {
		return accessTokenClaims.getSubject();
	}

	@Override
	public String getPrincipal() {
		return accessTokenClaims.getSubject();
	}
	
	@Override
	public String getDetails() {
		StringBuilder details = new StringBuilder();
		details.append("caller: ").append(accessTokenClaims.getSubject()).append(", ")
				.append("authorizations: [").append(Optional.ofNullable(this.scopes).orElse(new ArrayList<String>()).stream().collect(Collectors.joining(","))).append("]");
		return details.toString();
	}
	
	@Override
	public String getCredentials() {
		// don't return any credentials
		return null;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
	}

	@Override
	public boolean isAuthenticated() {
		return this.authenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		this.authenticated = isAuthenticated;
	}
	
	public String getUserIdToken(){
		return this.idToken;
	}
	
	public String getUserId() {
		return idTokenClaims.getSubject();
	}
	
	public String getUserDetails(){
		StringBuilder details = new StringBuilder();
		details.append("user: ").append(idTokenClaims.getSubject()).append(", ")
				.append("authentication type: ").append(idTokenClaims.get("amr")).append(", ")
				.append("customers: [").append(this.customers.stream().collect(Collectors.joining(","))).append("]");
		return details.toString();
	}
	
	public List<String> getCustomers() {
        return this.customers;
	}
	
	public boolean hasAuthority(String scope) {
		return scopes.stream().anyMatch(item -> item.equalsIgnoreCase(scope));
	}
	
	public AuthenticationType getAuthenticationType() {
		return AuthenticationType.fromValue((String)idTokenClaims.get("amr"));
	}
	
	public String getIdTokenClaim(String claim){
		return (String)idTokenClaims.get(claim);
	}
	
	public <T> T getIdTokenClaim(String claim, Class<T> type){
		return type.cast(idTokenClaims.get(claim));
	}
	
	public String getAccessTokenClaim(String claim){
		return (String)accessTokenClaims.get(claim);
	}
	
	public <T> T getAccessTokenClaim(String claim, Class<T> type){
		return type.cast(accessTokenClaims.get(claim));
	}
}
