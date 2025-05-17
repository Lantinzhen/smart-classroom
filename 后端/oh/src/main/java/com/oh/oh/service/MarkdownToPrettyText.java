package com.oh.oh.service;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Service;

@Service
public class MarkdownToPrettyText {

    public static String renderPretty(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        PrettyTextVisitor visitor = new PrettyTextVisitor();
        document.accept(visitor);
        return visitor.getText().trim();
    }

    static class PrettyTextVisitor extends AbstractVisitor {
        private final StringBuilder sb = new StringBuilder();

        @Override
        public void visit(Heading heading) {
            if (heading.getFirstChild() instanceof Text) {
                Text textNode = (Text) heading.getFirstChild();
                sb.append(textNode.getLiteral()).append("\n");
            } else {
                visitChildren(heading);
                sb.append("\n");
            }
        }

        @Override
        public void visit(Paragraph paragraph) {
            visitChildren(paragraph);
//            sb.append("\n");
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            visitChildren(strongEmphasis);
        }

        @Override
        public void visit(Emphasis emphasis) {
            visitChildren(emphasis);
        }

        @Override
        public void visit(BulletList bulletList) {
            visitChildren(bulletList);
        }

        @Override
        public void visit(ListItem listItem) {
            sb.append("  ");
//            sb.append(". ");
            visitChildren(listItem);
//            sb.append("\n");
        }

        @Override
        public void visit(Link link) {
            visitChildren(link); // 只保留文字
        }

        @Override
        public void visit(Text text) {
            sb.append(text.getLiteral());
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            sb.append("\n");
        }

        @Override
        public void visit(Code code) {
            sb.append(code.getLiteral());
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            sb.append("\n────────────\n");
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            sb.append("【引用】：");
            visitChildren(blockQuote);
            sb.append("\n");
        }

        @Override
        public void visit(HtmlInline htmlInline) {
            // 忽略或处理为纯文本
        }

        @Override
        public void visit(HtmlBlock htmlBlock) {
            // 同上
        }

        public String getText() {
            return sb.toString().trim();
        }
    }


    public static void main(String[] args) {
        String markdown = "## 温度记录\n\n**最新数据：**\n\n- 温度：23.5℃\n- 湿度：45%\n\n[点击查看详情](https://example.com)";
        String result = renderPretty(markdown);
        System.out.println(result);
    }
}
