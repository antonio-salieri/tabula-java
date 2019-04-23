package technology.tabula;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class ObjectExtractor {

    private final PDDocument pdfDocument;

    public ObjectExtractor(PDDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    protected Page extractPage(Integer pageNumber) throws IOException {

        if (pageNumber > this.pdfDocument.getNumberOfPages() || pageNumber < 1) {
            throw new java.lang.IndexOutOfBoundsException(
                    "Page number does not exist");
        }

        PDPage p = this.pdfDocument.getPage(pageNumber - 1);

        ObjectExtractorStreamEngine se = new ObjectExtractorStreamEngine(p);
        se.processPage(p);


        TextStripper pdfTextStripper = new TextStripper(this.pdfDocument, pageNumber);

        pdfTextStripper.process();

        // printTextElements(pdfTextStripper.textElements);
        Utils.sort(pdfTextStripper.textElements, Rectangle.ILL_DEFINED_ORDER);
        // printTextElements(pdfTextStripper.textElements);

        float w, h;
        int pageRotation = p.getRotation();
        if (Math.abs(pageRotation) == 90 || Math.abs(pageRotation) == 270) {
            w = p.getCropBox().getHeight();
            h = p.getCropBox().getWidth();
        } else {
            w = p.getCropBox().getWidth();
            h = p.getCropBox().getHeight();
        }

        return new Page(0, 0, w, h, pageRotation, pageNumber, p, pdfTextStripper.textElements,
                se.rulings, pdfTextStripper.minCharWidth, pdfTextStripper.minCharHeight, pdfTextStripper.spatialIndex);
    }

    private void printTextElements(List<TextElement> textElements) {
        StringBuilder sb = new StringBuilder();
        sb.append("printTextElements BEGIN\n");
        sb.append("[\n");
        for (TextElement el : textElements) {
            sb.append("{ ");
            
            sb.append("\"text\": \""+el.getText()+"\"");
            sb.append(", \"x\": "+el.getX());
            sb.append(", \"y\": "+el.getY());
            sb.append(", \"widht\": "+el.getWidth());
            sb.append(", \"height\": "+el.getHeight());
            sb.append(", \"dir\": "+el.getDirection());
            
            sb.append(" }\n");
        }
        sb.append("\n]");
        sb.append("printTextElements END");
        System.err.println(sb);
    }
    
    public PageIterator extract(Iterable<Integer> pages) {
        return new PageIterator(this, pages);
    }

    public PageIterator extract() {
        return extract(Utils.range(1, this.pdfDocument.getNumberOfPages() + 1));
    }

    public Page extract(int pageNumber) {
        return extract(Utils.range(pageNumber, pageNumber + 1)).next();
    }

    public void close() throws IOException {
        this.pdfDocument.close();
    }



}
