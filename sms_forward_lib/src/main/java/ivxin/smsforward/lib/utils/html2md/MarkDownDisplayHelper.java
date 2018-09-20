package ivxin.smsforward.lib.utils.html2md;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.loader.DefaultLoader;
import com.yydcdut.markdown.syntax.text.TextFactory;
import com.yydcdut.markdown.theme.ThemeDefault;

public class MarkDownDisplayHelper {
    private static MarkdownConfiguration markdownConfiguration;

    public static void display(@NonNull TextView textView, String content) {
        if (markdownConfiguration == null) {
            markdownConfiguration = new MarkdownConfiguration.Builder(textView.getContext())
                    .setHeader1RelativeSize(1.6f)//default relative size of header1
                    .setHeader2RelativeSize(1.5f)//default relative size of header2
                    .setHeader3RelativeSize(1.4f)//default relative size of header3
                    .setHeader4RelativeSize(1.3f)//default relative size of header4
                    .setHeader5RelativeSize(1.2f)//default relative size of header5
                    .setHeader6RelativeSize(1.1f)//default relative size of header6
                    .setBlockQuotesLineColor(Color.LTGRAY)//default color of block quotes line
                    .setBlockQuotesBgColor(Color.LTGRAY, Color.RED, Color.BLUE)//default color of block quotes background and nested background
//                    .setBlockQuotesRelativeSize(Color.LTGRAY, Color.RED, Color.BLUE)//default relative size of block quotes text size
                    .setHorizontalRulesColor(Color.LTGRAY)//default color of horizontal rules's background
                    .setHorizontalRulesHeight(Color.LTGRAY)//default height of horizontal rules
                    .setCodeFontColor(Color.LTGRAY)//default color of inline code's font
                    .setCodeBgColor(Color.LTGRAY)//default color of inline code's background
                    .setTheme(new ThemeDefault())//default code block theme
                    .setTodoColor(Color.DKGRAY)//default color of todo
                    .setTodoDoneColor(Color.DKGRAY)//default color of done
//                    .setOnTodoClickCallback(new OnTodoClickCallback() {//todo or done click callback
//                        @Override
//                        public CharSequence onTodoClicked(View view, String line) {
//                            return textView.getText();
//                        }
//                    })
                    .setUnOrderListColor(Color.BLACK)//default color of unorder list
                    .setLinkFontColor(Color.RED)//default color of link text
                    .showLinkUnderline(true)//default value of whether displays link underline
//                    .setOnLinkClickCallback(new OnLinkClickCallback() {//link click callback
//                        @Override
//                        public void onLinkClicked(View view, String link) {
//                        }
//                    })
                    .setRxMDImageLoader(new DefaultLoader(textView.getContext()))//default image loader
                    .setDefaultImageSize(100, 100)//default image width & height
                    .build();
        }
        MarkdownProcessor markdownProcessor = new MarkdownProcessor(textView.getContext());
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(markdownConfiguration);
        textView.setText(markdownProcessor.parse(content));
    }
}
