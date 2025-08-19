package com.javajedis.legalconnect.videocall;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom exception for JWT token generation errors.
 */
class JwtTokenGenerationException extends Exception {
    public JwtTokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public JwtTokenGenerationException(String message) {
        super(message);
    }
}

/**
 * The core code is copied from
 * <a href="https://github.com/8x8/jaas_demo/blob/main/jaas-jwt-samples/java/src/main/java/com/_8x8_/Main.java">...</a>
 * Updated for latest JWT library and service requirements
 */
@Component
@Slf4j
public class JitsiJwtGenerator {

    @Value("${jaas.app-id}")
    private String jaasAppId;

    @Value("${jaas.api-key}")
    private String jaasApiKey;

    /**
     * Placeholder helper string.
     */
    public static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    /**
     * Placeholder helper string.
     */
    public static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    /**
     * Placeholder empty string.
     */
    public static final String EMPTY = "";
    /**
     * Helper string for key algorithm.
     */
    public static final String RSA = "RSA";
    /**
     * To be used with exp value.
     * The time after which the JWT expires.
     */
    public static final long EXP_TIME_DELAY_SEC = 7200;
    /**
     * To be used with nbf value.
     * The time before which the JWT must not be accepted for processing.
     */
    public static final long NBF_TIME_DELAY_SEC = 10;
    /**
     * The file that contains the generated rsa private key. See https://jaas.8x8.vc/#/start-guide Section 1. API Keys
     * The file must be in PKCS #8 format supported by JAVA, also see https://tools.ietf.org/html/rfc5208.
     * Use the following command to convert the generated private key to PKCS #8 :
     * openssl pkcs8 -topk8 -inform PEM -in <input key path> -outform pem -nocrypt -out <output key path>
     */
    private static final String PRIVATE_KEY_FILE_RSA = "src/main/resources/jaas-key.pk";

    /**
     * Reads a private key from a file in PKCS #8 format.
     *
     * @param filename the path to the private key file
     * @return the RSA private key
     * @throws JwtTokenGenerationException if there's an error reading or parsing the key
     */
    public static RSAPrivateKey getPemPrivateKey(String filename) throws JwtTokenGenerationException {
        try {
            String pem = new String(Files.readAllBytes(Paths.get(filename)));
            String privKey = pem.replace(BEGIN_PRIVATE_KEY, EMPTY)
                               .replace(END_PRIVATE_KEY, EMPTY)
                               .replaceAll("\\s+", ""); // Remove all whitespace including newlines
            byte[] decoded = Base64.getDecoder().decode(privKey);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance(RSA);
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Failed to read or parse private key from file: " + filename, e);
        }
    }

    /**
     * Generates a JaaS JWT token with the provided parameters including room name.
     * Premium features (recording, transcription, live streaming, outbound calls) are disabled for free tier.
     * isModerator is set to true by default, userAvatar is set to null by default.
     *
     * @param apiKey the JaaS API key
     * @param userName the user's display name
     * @param userEmail the user's email address
     * @param roomName the specific room name for this token
     * @param isModerator whether the user is a moderator
     * @return the generated JWT token
     * @throws JwtTokenGenerationException if there's an error generating the token
     */
    public String generateJaasToken(String apiKey, String userName, String userEmail, String roomName,
                                   boolean isModerator) throws JwtTokenGenerationException {
        RSAPrivateKey rsaPrivateKey = getPemPrivateKey(PRIVATE_KEY_FILE_RSA);

        // Log the token generation parameters for debugging
        log.debug("Generating JaaS token for user: {}, email: {}, room: {}, moderator: {}",
                 userName, userEmail, roomName, isModerator);

        JaaSJwtBuilder builder = JaaSJwtBuilder.builder()
                .withDefaults()
                .withApiKey(apiKey)
                .withUserName(userName)
                .withUserEmail(userEmail)
                .withRoomName(roomName) // Set specific room instead of wildcard
                .withModerator(true)
                .withAppID(jaasAppId);

        String token = builder.signWith(rsaPrivateKey);
        log.debug("Generated JaaS token successfully for room: {}", roomName);
        return token;
    }

    /**
     * Generates a JaaS JWT token with minimal required parameters including room name.
     * isModerator is set to true by default, userAvatar is set to null by default.
     *
     * @param apiKey the JaaS API key
     * @param userName the user's display name
     * @param userEmail the user's email address
     * @param roomName the specific room name for this token
     * @return the generated JWT token
     * @throws JwtTokenGenerationException if there's an error generating the token
     */
    public String generateJaasToken(String apiKey, String userName, String userEmail, String roomName) throws JwtTokenGenerationException {
        // Default to moderator for all users
        return generateJaasToken(apiKey, userName, userEmail, roomName, true);
    }

    /**
     * Generates a JaaS JWT token using the configured API key with room name.
     * isModerator is set to true by default, userAvatar is set to null by default.
     *
     * @param userName the user's display name
     * @param userEmail the user's email address
     * @param roomName the specific room name for this token
     * @return the generated JWT token
     * @throws JwtTokenGenerationException if there's an error generating the token
     */
    public String generateJaasTokenWithConfig(String userName, String userEmail, String roomName) throws JwtTokenGenerationException {
        // Always set as moderator regardless of other settings
        return generateJaasToken(jaasApiKey, userName, userEmail, roomName, true);
    }

    /**
     * Generates a JaaS JWT token using the configured API key for a lawyer (moderator) in a specific room.
     * isModerator is set to true by default, userAvatar is set to null by default.
     *
     * @param lawyerName the lawyer's display name
     * @param lawyerEmail the lawyer's email address
     * @param roomName the specific room name for this token
     * @return the generated JWT token
     * @throws JwtTokenGenerationException if there's an error generating the token
     */
    public String generateLawyerToken(String lawyerName, String lawyerEmail, String roomName) throws JwtTokenGenerationException {
        // Temporarily set all users as moderators to test if this resolves the "no moderator arrived" issue
        return generateJaasToken(jaasApiKey, lawyerName, lawyerEmail, roomName, true);
    }

    /**
     * Generates a JaaS JWT token using the configured API key for a client (non-moderator) in a specific room.
     * isModerator is set to true by default, userAvatar is set to null by default.
     *
     * @param clientName the client's display name
     * @param clientEmail the client's email address
     * @param roomName the specific room name for this token
     * @return the generated JWT token
     * @throws JwtTokenGenerationException if there's an error generating the token
     */
    public String generateClientToken(String clientName, String clientEmail, String roomName) throws JwtTokenGenerationException {
        // Set clients as moderators too to ensure everyone can start meetings
        return generateJaasToken(jaasApiKey, clientName, clientEmail, roomName, true);
    }

    // Keep the old methods for backward compatibility
    /**
     * Generates a JaaS JWT token using the configured API key with minimal parameters.
     * Uses wildcard room for backward compatibility.
     */
    public String generateJaasTokenWithConfig(String userName, String userEmail) throws JwtTokenGenerationException {
        return generateJaasToken(jaasApiKey, userName, userEmail, "*", true);
    }

    /**
     * Generates a JaaS JWT token for a lawyer using wildcard room.
     * For backward compatibility - consider using the version with specific roomName.
     */
    public String generateLawyerToken(String lawyerName, String lawyerEmail) throws JwtTokenGenerationException {
        return generateJaasToken(jaasApiKey, lawyerName, lawyerEmail, "*", true);
    }

    /**
     * Generates a JaaS JWT token for a client using wildcard room.
     * For backward compatibility - consider using the version with specific roomName.
     */
    public String generateClientToken(String clientName, String clientEmail) throws JwtTokenGenerationException {
        // Set clients as moderators too to ensure everyone can start meetings
        return generateJaasToken(jaasApiKey, clientName, clientEmail, "*", true);
    }

    public static void main(String[] args) {
        try {
            /** Read private key from file. */
            RSAPrivateKey rsaPrivateKey = getPemPrivateKey(PRIVATE_KEY_FILE_RSA);

            /** Create new JaaSJwtBuilder and setup the claims. */
            String token = JaaSJwtBuilder.builder()
                    .withDefaults() // This sets default/most common values
                    .withApiKey("vpaas-magic-cookie-0b2313ac61a749f09df6e9c27e20c190/7b9e32") // Set the api key from config
                    .withUserName("My name here") // Set the user name
                    .withUserEmail("My email here") // Set the user email
                    .withModerator(true) // Enable user as moderator
                    .withAppID("vpaas-magic-cookie-0b2313ac61a749f09df6e9c27e20c190") // Set the AppID from config
                    .signWith(rsaPrivateKey); /** Finally the JWT is signed with the private key */

            log.info("Generated JaaS JWT token: {}", token);
        } catch (Exception ex) {
            log.error("Error generating JaaS JWT token: {}", ex.getMessage(), ex);
        }
    }

    /**
     * JaaSJwtBuilder class that helps generate JaaS tokens using JJWT library.
     */
    static class JaaSJwtBuilder {
        private String apiKey;
        private Long expTime;
        private Long nbfTime;
        private String roomName;
        private String appId;
        private Map<String, Object> userClaims;
        private Map<String, Object> featureClaims;

        private JaaSJwtBuilder() {
            userClaims = new HashMap<>();
            featureClaims = new HashMap<>();
        }

        /**
         * Creates a new JaaSJwtBuilder.
         *
         * @return Returns a new builder that needs to be setup.
         */
        public static JaaSJwtBuilder builder() {
            return new JaaSJwtBuilder();
        }

        /**
         * Sets the value for the kid header claim. Represents the JaaS api key.
         * You can find the api key here : https://jaas.8x8.vc/#/apikeys
         *
         * @param apiKey the API key
         * @return Returns a new builder with kid claim set.
         */
        public JaaSJwtBuilder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets the value for the user avatar url as a string.
         *
         * @param url The publicly available URL that points to the user avatar picture.
         * @return Returns a new builder with avatar claim set.
         */
        public JaaSJwtBuilder withUserAvatar(String url) {
            userClaims.put("avatar", url);
            return this;
        }

        /**
         * Sets the value for the moderator claim. If value is true user is moderator, false otherwise.
         *
         * @param isModerator whether the user is a moderator
         * @return Returns a new builder with moderator claim set.
         */
        public JaaSJwtBuilder withModerator(boolean isModerator) {
            userClaims.put("moderator", String.valueOf(isModerator));
            return this;
        }

        /**
         * Sets the value for the user name to be displayed in the meeting.
         *
         * @param userName the user's display name
         * @return Returns a new builder with name claim set.
         */
        public JaaSJwtBuilder withUserName(String userName) {
            userClaims.put("name", userName);
            return this;
        }

        /**
         * Sets the value for the user email claim.
         *
         * @param userEmail the user's email
         * @return Returns a new builder with email claim set.
         */
        public JaaSJwtBuilder withUserEmail(String userEmail) {
            userClaims.put("email", userEmail);
            return this;
        }

        /**
         * Sets the value for the exp claim representing the time until the token expires.
         *
         * @param expTime Unix timestamp is expected.
         * @return Returns a new builder with exp claim set.
         */
        public JaaSJwtBuilder withExpTime(long expTime) {
            this.expTime = expTime;
            return this;
        }

        /**
         * Sets the value for the nbf claim.
         *
         * @param nbfTime Unix timestamp is expected.
         * @return Returns a new builder with nbf claim set.
         */
        public JaaSJwtBuilder withNbfTime(long nbfTime) {
            this.nbfTime = nbfTime;
            return this;
        }

        /**
         * Sets the value for the room.
         *
         * @param roomName The meeting room value for which the token is issued;
         *                 this field supports also wildcard ("*") if the token is issued for all rooms.
         * @return Returns a new builder with room claim set.
         */
        public JaaSJwtBuilder withRoomName(String roomName) {
            this.roomName = roomName;
            return this;
        }

        /**
         * Sets the value for the sub claim representing the AppID (previously tenant name/unique identifier).
         *
         * @param appId The AppID that identifies your application.
         * @return Returns a new builder with sub claim set.
         */
        public JaaSJwtBuilder withAppID(String appId) {
            this.appId = appId;
            return this;
        }

        /**
         * Sets the value for the id claim.
         *
         * @param userId The user's unique identifier.
         * @return Returns a new builder with user id claim set.
         */
        public JaaSJwtBuilder withUserId(String userId) {
            userClaims.put("id", userId);
            return this;
        }

        /**
         * Fills the default values for required claims.
         *
         * @return Returns a new builder with needed claim set to default values.
         */
        public JaaSJwtBuilder withDefaults() {
            return this.withExpTime(Instant.now().getEpochSecond() + EXP_TIME_DELAY_SEC)
                    .withNbfTime(Instant.now().getEpochSecond() - NBF_TIME_DELAY_SEC)
                    .withModerator(true)
                    .withUserId(UUID.randomUUID().toString());
        }

        /**
         * Returns a signed JaaS JWT token string using JJWT library.
         *
         * @param privateKey The private key used to sign the JWT.
         * @return A signed JWT.
         */
        public String signWith(RSAPrivateKey privateKey) {
            Map<String, Object> context = new HashMap<>();
            context.put("user", userClaims);
            context.put("features", featureClaims);

            return Jwts.builder()
                    .header()
                        .keyId(apiKey)
                        .and()
                    .issuer("chat")
                    .audience().add("jitsi").and()
                    .subject(appId)
                    .claim("room", roomName)
                    .claim("context", context)
                    .expiration(expTime != null ? Date.from(Instant.ofEpochSecond(expTime)) : null)
                    .notBefore(nbfTime != null ? Date.from(Instant.ofEpochSecond(nbfTime)) : null)
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        }
    }

}

