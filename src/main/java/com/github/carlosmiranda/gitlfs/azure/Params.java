package com.github.carlosmiranda.gitlfs.azure;

import java.util.Properties;

/**
 * Properties parameters.
 */
final class Params {
    /**
     * HTTP Basic username.
     */
    private final String user;
    /**
     * HTTP Basic password.
     */
    private final String pass;
    /**
     * HTTP Basic realm.
     */
    private final String rlm;
    /**
     * Azure Storage account name.
     */
    private final String acct;
    /**
     * Azure Storage container.
     */
    private final String cntr;
    /**
     * Azure storage key.
     */
    private final String azkey;
    /**
     * Server path.
     */
    private final String pth;
    /**
     * Server port.
     */
    private final int prt;
    /**
     * Ctor.
     * @param props Properties
     */
    Params(final Properties props) {
        this.user = props.getProperty("gitlfs.username");
        this.pass = props.getProperty("gitlfs.password");
        this.rlm = props.getProperty("gitlfs.realm");
        this.pth = props.getProperty("gitlfs.path");
        this.prt = Integer.parseInt(props.getProperty("gitlfs.port"));
        this.acct = props.getProperty("azure.account");
        this.azkey = props.getProperty("azure.key");
        this.cntr = props.getProperty("azure.container");
    }
    /**
     * Git LFS username.
     * @return Username
     */
    public String username() {
        return this.user;
    }
    /**
     * Git LFS Password.
     * @return Password
     */
    public String password() {
        return this.pass;
    }
    /**
     * Authentication realm.
     * @return HTTP Authentication realm.
     */
    public String realm() {
        return this.rlm;
    }
    /**
     * Git LFS path.
     * @return Git LFS path
     */
    public String path() {
        return this.pth;
    }
    /**
     * Azure account.
     * @return Azure account
     */
    public String account() {
        return this.acct;
    }
    /**
     * Azure Key.
     * @return Azure key
     */
    public String key() {
        return this.azkey;
    }
    /**
     * Azure container name.
     * @return Azure container.
     */
    public String container() {
        return this.cntr;
    }
    /**
     * Port.
     * @return Server port
     */
    public int port() {
        return this.prt;
    }
}
