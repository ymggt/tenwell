package org.tenwell.identity.agent.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tenwell.identity.agent.exception.SSOAgentException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

public class SSOAgentX509KeyStoreCredential implements SSOAgentX509Credential {

    private static final Log log = LogFactory.getLog(SSOAgentX509KeyStoreCredential.class);
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;
    private X509Certificate entityCertificate = null;

    public SSOAgentX509KeyStoreCredential(KeyStore keyStore, String publicCertAlias,
                                          String privateKeyAlias, char[] privateKeyPassword)
            throws SSOAgentException {

        readX509Credentials(keyStore, publicCertAlias, privateKeyAlias, privateKeyPassword);
    }

    public SSOAgentX509KeyStoreCredential(InputStream keyStoreInputStream, char[] keyStorePassword,
                                          String publicCertAlias, String privateKeyAlias,
                                          char[] privateKeyPassword)
            throws SSOAgentException {

        readX509Credentials(keyStoreInputStream, keyStorePassword, publicCertAlias,
                privateKeyAlias, privateKeyPassword);
    }

    @Override
    public PublicKey getPublicKey() throws SSOAgentException {
        return publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() throws SSOAgentException {
        return privateKey;
    }

    @Override
    public X509Certificate getEntityCertificate() throws SSOAgentException {
        return entityCertificate;
    }

    protected void readX509Credentials(KeyStore keyStore, String publicCertAlias,
                                       String privateKeyAlias, char[] privateKeyPassword)
            throws SSOAgentException {

        try {
            entityCertificate = (X509Certificate) keyStore.getCertificate(publicCertAlias);
        } catch (KeyStoreException e) {
            throw new SSOAgentException(
                    "Error occurred while retrieving public certificate for alias " +
                            publicCertAlias, e);
        }
        publicKey = entityCertificate.getPublicKey();
        try {
            privateKey = (PrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPassword);
        } catch (KeyStoreException e) {
            throw new SSOAgentException(
                    "Error occurred while retrieving private key for alias " +
                            privateKeyAlias, e);
        } catch (NoSuchAlgorithmException e) {
            throw new SSOAgentException(
                    "Error occurred while retrieving private key for alias " +
                            privateKeyAlias, e);
        } catch (UnrecoverableKeyException e) {
            throw new SSOAgentException(
                    "Error occurred while retrieving private key for alias " +
                            privateKeyAlias, e);
        }
    }

    protected void readX509Credentials(InputStream keyStoreInputStream, char[] keyStorePassword,
                                       String publicCertAlias, String privateKeyAlias,
                                       char[] privateKeyPassword)
            throws SSOAgentException {

        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreInputStream, keyStorePassword);
            readX509Credentials(keyStore, publicCertAlias, privateKeyAlias, privateKeyPassword);
        } catch (Exception e) {
            throw new SSOAgentException("Error while loading key store file", e);
        } finally {
            if (keyStoreInputStream != null) {
                try {
                    keyStoreInputStream.close();
                } catch (IOException ignored) {
                    if (log.isDebugEnabled()){
                        log.debug("Ignoring IO Exception : ", ignored);
                    }
                    throw new SSOAgentException("Error while closing input stream of key store");
                }
            }
        }
    }
}
