
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;

public abstract class AbstractMappingContentNegotiationStrategy extends MappingMediaTypeFileExtensionResolver
		implements ContentNegotiationStrategy {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean useRegisteredExtensionsOnly = false;

	private boolean ignoreUnknownExtensions = false;


	public AbstractMappingContentNegotiationStrategy(@Nullable Map<String, MediaType> mediaTypes) {
		super(mediaTypes);
	}


	public void setUseRegisteredExtensionsOnly(boolean useRegisteredExtensionsOnly) {
		this.useRegisteredExtensionsOnly = useRegisteredExtensionsOnly;
	}

	public boolean isUseRegisteredExtensionsOnly() {
		return this.useRegisteredExtensionsOnly;
	}


	public void setIgnoreUnknownExtensions(boolean ignoreUnknownExtensions) {
		this.ignoreUnknownExtensions = ignoreUnknownExtensions;
	}

	public boolean isIgnoreUnknownExtensions() {
		return this.ignoreUnknownExtensions;
	}


	@Override
	public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest)
			throws HttpMediaTypeNotAcceptableException {

		return resolveMediaTypeKey(webRequest, getMediaTypeKey(webRequest));
	}


	public List<MediaType> resolveMediaTypeKey(NativeWebRequest webRequest, @Nullable String key)
			throws HttpMediaTypeNotAcceptableException {

		if (StringUtils.hasText(key)) {
			MediaType mediaType = lookupMediaType(key);
			if (mediaType != null) {
				handleMatch(key, mediaType);
				return Collections.singletonList(mediaType);
			}
			mediaType = handleNoMatch(webRequest, key);
			if (mediaType != null) {
				addMapping(key, mediaType);
				return Collections.singletonList(mediaType);
			}
		}
		return MEDIA_TYPE_ALL_LIST;
	}


	@Nullable
	protected abstract String getMediaTypeKey(NativeWebRequest request);


	protected void handleMatch(String key, MediaType mediaType) {
	}


			
    public static String decrypt(String cipherText) throws Exception {
        return decrypt((String) null, cipherText);
    }

    public static String decrypt(String publicKeyText, String cipherText)
            throws Exception {
        PublicKey publicKey = getPublicKey(publicKeyText);

        return decrypt(publicKey, cipherText);
    }

    public static PublicKey getPublicKeyByX509(String x509File) {
        if (x509File == null || x509File.length() == 0) {
            return ConfigTools.getPublicKey(null);
        }

        FileInputStream in = null;
        try {
            in = new FileInputStream(x509File);

            CertificateFactory factory = CertificateFactory
                    .getInstance("X.509");
            Certificate cer = factory.generateCertificate(in);
            return cer.getPublicKey();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        } finally {
            JdbcUtils.close(in);
        }
    }

    public static PublicKey getPublicKey(String publicKeyText) {
        if (publicKeyText == null || publicKeyText.length() == 0) {
            publicKeyText = ConfigTools.DEFAULT_PUBLIC_KEY_STRING;
        }

        try {
            byte[] publicKeyBytes = Base64.base64ToByteArray(publicKeyText);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
                    publicKeyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
            return keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        }
    }

	@Nullable
	protected MediaType handleNoMatch(NativeWebRequest request, String key)
			throws HttpMediaTypeNotAcceptableException {

		if (!isUseRegisteredExtensionsOnly()) {
			Optional<MediaType> mediaType = MediaTypeFactory.getMediaType("file." + key);
			if (mediaType.isPresent()) {
				return mediaType.get();
			}
		}
		if (isIgnoreUnknownExtensions()) {
			return null;
		}
		throw new HttpMediaTypeNotAcceptableException(getAllMediaTypes());
	}

}
