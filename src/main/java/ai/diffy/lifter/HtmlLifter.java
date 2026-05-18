package ai.diffy.lifter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HtmlLifter {

    public static FieldMap lift(Element node) {
        if (node instanceof Document doc) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("head", lift(doc.head()));
            m.put("body", lift(doc.body()));
            return new FieldMap(m);
        } else {
            Map<String, Object> attrMap = new LinkedHashMap<>();
            node.attributes().asList().forEach(attr -> attrMap.put(attr.getKey(), attr.getValue()));
            FieldMap attributes = new FieldMap(attrMap);

            List<FieldMap> children = StreamSupport.stream(node.children().spliterator(), false)
                .map(HtmlLifter::lift)
                .collect(Collectors.toList());

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tag", node.tagName());
            m.put("text", node.ownText());
            m.put("attributes", attributes);
            m.put("children", children);
            return new FieldMap(m);
        }
    }

    public static Document decode(String html) {
        return Jsoup.parse(html);
    }
}
