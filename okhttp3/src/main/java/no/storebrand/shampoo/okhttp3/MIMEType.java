package no.storebrand.shampoo.okhttp3;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MIMEType {
    public static final MIMEType ALL = new MIMEType("*", "*");
    public static final MIMEType APPLICATION_OCTET_STREAM = new MIMEType("application", "octet-stream");

    private final String type;
    private final String subType;
    private final Map<String, String> parameters;

    public MIMEType(String primaryType, String subType) {
        this(primaryType, subType, Collections.emptyMap());
    }

    private MIMEType(String primaryType, String subType, Map<String, String> parameters) {
        this.type = primaryType;
        this.subType = subType;
        this.parameters = parameters;
    }

    /**
     * Adds a parameter to the MIMEType.
     *
     * @param name  name of parameter
     * @param value value of parameter
     * @return returns a new instance with the parameter set
     */
    public MIMEType addParameter(String name, String value) {
        Map<String, String> copy = new LinkedHashMap<>(this.parameters);
        copy.put(name, value);
        return new MIMEType(type, subType, copy);
    }

    public String getSubType() {
        return subType;
    }

    public String getPrimaryType() {
        return type;
    }

    public String getCharset() {
        return parameters.get("charset");
    }

    @Override
    public int hashCode() {
        return 31 * (getPrimaryType().hashCode() + getSubType().hashCode());
    }

    @Override
    public boolean equals(final Object object) {
        return equals(object, true);
    }

    public boolean equalsWithoutParameters(final Object o) {
        return equals(o, false);
    }

    private boolean equals(final Object o, final boolean includeParameters) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MIMEType other = (MIMEType) o;
        if (!(getPrimaryType().equals(other.getPrimaryType()) && getSubType().equals(other.getSubType()))) {
            return false;
        }
        if (includeParameters && !parametersEquals(other)) {
            return false;
        }

        return true;
    }

    private boolean parametersEquals(MIMEType other) {
        return this.parameters.equals(other.parameters);
    }

    public boolean includes(MIMEType mimeType) {
        boolean includes = mimeType == null || equalsWithoutParameters(ALL) || equalsWithoutParameters(mimeType);
        if (!includes) {
            includes = getPrimaryType().equals(mimeType.getPrimaryType())
                    && (getSubType().equals(mimeType.getSubType()) || "*".equals(getSubType()));
        }
        return includes;
    }

    public boolean includes(String mimeType) {
        return includes(valueOf(mimeType));
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        String base = String.format("%s/%s", type, subType);
        return base + (parameters.isEmpty() ? "" : parameters.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";", ";", "")));
    }

    public static MIMEType valueOf(final String MIMEType) {
        Pattern pattern = Pattern.compile("([\\w-*]+)/([\\w-*+.]+);?(.*)?", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(MIMEType);
        if (matcher.matches()) {
            Map<String, String> map = new LinkedHashMap<>();
            String type = matcher.group(1);
            String subtype = matcher.group(2);
            if (matcher.groupCount() == 3) {
                String params = matcher.group(3);
                parseParams(params, map);
            }
            return new MIMEType(type, subtype, Collections.unmodifiableMap(map));
        } else {
            throw new IllegalArgumentException("Not a valid MIMEType");
        }
    }

    private static void parseParams(String params, Map<String, String> map) {
        Pattern equalPattern = Pattern.compile("(.*)=(.*)", Pattern.MULTILINE | Pattern.DOTALL);
        Scanner scanner = new Scanner(params).useDelimiter(";");
        while (scanner.hasNext()) {
            String next = scanner.next();
            Matcher matcher = equalPattern.matcher(next);
            if (matcher.matches()) {
                String name = matcher.group(1).trim();
                String value = matcher.group(2).trim();
                map.put(name, value);
            } else {
                throw new IllegalArgumentException(next + "was not a valid formatted parameter");
            }
        }
    }

    public static MIMEType valueOf(final String primaryType, final String subType) {
        return new MIMEType(primaryType, subType);
    }

    public static boolean includes(String mimeType1, String mimeType2) {
        return valueOf(mimeType1).includes(MIMEType.valueOf(mimeType2));
    }
}
