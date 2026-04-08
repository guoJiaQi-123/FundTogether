package com.fundtogether.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {

    public static final String APP_ID = "9021000136637271";
    public static final String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDewbuTuN+1v5d+5L8kwGjNRmuFLLDVT4bSnaLchSiLQcTTN93VQO3iwZ0OfsW7xO5q9SEnribWVR8gjd6k+N/QsOAAWgw7k9hqTVOBrJkDG5ExXK2oUj6xQNQOM25x807c5EY2Zfki1DjSTFCAXfUBNT9/WY46FSkbLlM5SGhcSjPMyawLMFL8IwIaSzTzM4GAYHtkZ1AV34N8v+D6ckR4Qp+OtokcpqCdVue5d1GTE9h2/SvFpeWU4woIuFPALhkZajOdIURG+JrWjI5EGGQdNNg+DmZX2mbHaQ9ocBZ7RxfhIvq/rFzaV4bSCDk58MmehJPVanFFImuKmdGTR7IpAgMBAAECggEAbU9kXPk7zlRWY06c/cvtoc4MtS1ZGVQNNQ1l/hDZ9HwxufSQRzW1hrC8AYD0R1A8LwBbZL1xEXxz6eW0fAAk1KP3SDToC9RhPsjzUY8BByVf+nkgPpkdap4FcXKLoB9lc8ARNGCWASFjQM9yMYvpzs3yeuvXf2ki4tO4w0Nk8uQMVIXeobL57p2TvBGANpPz/cHSrpSGjKJ+BDej2FzSp15ScB1N75dHPkcZ7f417zg39g7oVBP3qVg3FTcLFrKAVw49aGJDpeM3iTcPALcDeox7SfcUYmcHnUIHJjQ/PqzpRSgQuodI0TW8R+4sTSvmgWgAKJ+0zMR/UFBZoOnqAQKBgQD1Eb9hHDPpBR5/lYYEQV8nOc/kDT5Z/h8kDWk2vGfu6G96paVYhU4QRYhF9IFYE8HruwFazBtHxeHvR07GQkn6yMWpzONXyOZtRHyR3MuGk3hNyYE/K9apMHbPvicgNCGAOlff+Wh6gZbDQS73DcJRFuSFcMTMluwbnf9i/HJzzQKBgQDosTc6D7SdqWTHFzI+IX5s/ALVG8Ba9Ib5fa+AFcnM/sAWyRnds9Vz5ZNXuxbNk9cW573uPpoPsVRlicT/Prz2PzSM8d5OPhR9tdzZD+GXmmvfnCR5PuafPjmJeGUxeoE7CsNJ4NH+rlpBrIfW+EeIN4aSNYYT5YqHzGIO7evTzQKBgQCc65xLpi9nCawZrbh+iQ1QpKgFMqW2Ig32m8+/CKzUQWkAc4iIQfNWX+9+O62GpEALnVakuQTO1FpzSImcUV3lgyFuNI9t6moBlVTeYS7BITRrOpfTx1JQS4QISY3sBPVekf0qoRzRNKFArUBRWTIPkkUJXMfm6MyOcoDQ9SykEQKBgHb4xOfmx84koeY5MfMaZO2MHFahjcPzal99p5GxkjBBwCzmPVdhby2Gn8MnYQ1THZIUgsOhNlv3qtMPka1/glXVEyVNNQCENS3hIj3JJOrChY0a5HspcfjRc9GZwQN/QzvQ0PChBj1VH8icUgfTgZ9f5Go0+4yIvD+afDht5TmxAoGBAJqzCWerB5KTn3dui/4NA/7+5CcAUsDiz9aCv2Ue7Mz/bsZ8540EYegdoM71bmxYGlxgVt4pBMIjChFbmh112B6aRZe2RJOxB7REvowN4/Vydvd5lg4NWxaER1WgRGQBXQNsWa268r88Y5zDzAK2uyWCDFqpyHUSgWugVEjHgp3h";
    public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtNayvbg7SYE5kBYRBBaqaVE4UJMJ46H9wY84ftZU8cWiWQgBq/FY77FQcAHyNK7Tq5MW6b4pXd9Cscee+2yE8myff9OpDC8KCHrd2tJeAnXITLZ37vf5FsO867Tf9WEvRmDu6POo5OaaeXRZR7jp8jiDhD2BAE8qX/oK7aJr+VXe4W75KbHDHMC5vW94gUH7HPkofrG6eb3LExQGfJMNA7E6T9eLi45YUi4Sqm60BcZ0ARh0nXL8TcWYAWmAFyU3Ku6H8nqZ4xEu7DMoXpWe547cxMFFvMAVZfF6Zir3hYW1YEoaWNxPkDCG7G37QlXPkUCmtNf64oFkh2nbxtO31wIDAQAB";
    public static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    public static final String FORMAT = "JSON";
    public static final String CHARSET = "UTF-8";
    public static final String SIGN_TYPE = "RSA2";

    // You should modify the domain dynamically or use localhost.
    // Usually for notify_url it requires a public IP, but for testing we will
    // handle it locally or via ngrok
    // Here we'll configure a dummy public url or use localhost.
    public static final String NOTIFY_URL = "http://localhost:8080/api/user/account/alipay/notify";
    public static final String RETURN_URL = "http://localhost:8080/api/user/account/alipay/return"; // redirect back to
                                                                                                    // Vue app via
                                                                                                    // backend

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY,
                SIGN_TYPE);
    }
}