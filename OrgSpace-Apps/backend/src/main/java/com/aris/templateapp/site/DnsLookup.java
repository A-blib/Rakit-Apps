package com.aris.templateapp.site;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/** Pembacaan DNS untuk domain kustom. Dipisah agar test bisa memakai jawaban DNS palsu. Gagal = jawaban kosong. */
@Slf4j
@Component
public class DnsLookup {

    /** Isi record TXT (tanpa tanda kutip). */
    public List<String> txt(String name) {
        List<String> values = new ArrayList<>();
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "2500");
            env.put("com.sun.jndi.dns.timeout.retries", "1");
            Attributes attrs = new InitialDirContext(env).getAttributes(name, new String[]{"TXT"});
            Attribute txt = attrs.get("TXT");
            for (int i = 0; txt != null && i < txt.size(); i++) {
                values.add(txt.get(i).toString().replace("\"", "").trim());
            }
        } catch (Exception e) {
            log.debug("Pembacaan TXT {} gagal: {}", name, e.getMessage());
        }
        return values;
    }

    /** Alamat IPv4 tujuan nama ini (CNAME diikuti otomatis oleh resolver). */
    public Set<String> addresses(String name) {
        Set<String> ips = new HashSet<>();
        try {
            for (InetAddress address : InetAddress.getAllByName(name)) {
                if (address instanceof Inet4Address) {
                    ips.add(address.getHostAddress());
                }
            }
        } catch (Exception e) {
            log.debug("Pembacaan alamat {} gagal: {}", name, e.getMessage());
        }
        return ips;
    }
}
