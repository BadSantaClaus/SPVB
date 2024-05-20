package constants;


import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties",
        "classpath:config/${env}.properties"})
public interface ICredentials extends Config {
    @Key("bank.quik.login")
    String bankQuikLogin();

    @Key("bank.quik.password")
    String bankQuikPassword();

    @Key("cb.quik.login")
    String cbQuikLogin();

    @Key("cb.quik.password")
    String cbQuikPassword();

    @Key("db.url")
    String dbUrl();
    @Key("db.ark.url")
    String dbArkUrl();
    @Key("db.ark.QExport.url")
    String dbArkQExportUrl();
    @Key("db.ark.login")
    String dbArkLogin();

    @Key("db.ark.password")
    String dbArkPassword();

    @Key("db.login")
    String dbLogin();

    @Key("db.password")
    String dbPassword();

    @Key("ssh.remote.host")
    String sshHost();

    @Key("ssh.remote.port")
    int sshPort();

    @Key("ssh.sftp.login")
    String sshSftpLogin();

    @Key("ssh.crypto.login")
    String sshCryptoLogin();

    @Key("ssh.login.password")
    String sshLoginPassword();

    @Key("ssh.underwrite.password")
    String sshUnderWritePassword();

    @Key("web.url")
    String webUrl();

    @Key("web.login")
    String webLoginUser();

    @Key("web.password")
    String webPasswordUser();

    @Key("bootstrap.server")
    String bootstrapServer();
}
