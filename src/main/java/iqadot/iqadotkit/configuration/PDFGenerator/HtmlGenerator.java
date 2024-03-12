package iqadot.iqadotkit.configuration.PDFGenerator;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PDFDomTreeConfig;
import org.springframework.web.bind.annotation.PostMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HtmlGenerator {

    // return html in string
    public static void generateHtmlFromPdf(InputStream inputStream) throws IOException, ParserConfigurationException {
        PDDocument pdf = PDDocument.load(inputStream);
        PDFDomTree parser = new PDFDomTree();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer output = new PrintWriter("src/output/pdf.html", "utf-8");
        parser.writeText(pdf, output);
        output.close();
        pdf.close();
    }

    public static String generateHtmlFromPdf2(InputStream inputStream) throws IOException, ParserConfigurationException {
        PDDocument pdf = PDDocument.load(inputStream);
        PDFDomTree parser = new PDFDomTree();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer output = new PrintWriter(baos, true, StandardCharsets.UTF_8);
        parser.writeText(pdf, output);
        output.close();
        pdf.close();
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }


    public static void generateImageFromPDF(InputStream inputStream, String extension) throws IOException {
        PDDocument document = PDDocument.load(inputStream);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    page, 300, ImageType.RGB);
            File outputFile = new File(String.format("src/output/pdf-%d.%s", page + 1, extension));
            ImageIO.write(bim, extension, outputFile);
        }
        document.close();
    }

    public static String convertPdfToText(InputStream inputStream) throws IOException {
        PDDocument document = PDDocument.load(inputStream);
//        PDFRenderer pdfRenderer = new PDFRenderer(document);
        COSDocument cosDoc = document.getDocument();
        PDFTextStripper pdfStripper = new PDFTextStripper();
        PDDocument pdDoc = new PDDocument(cosDoc);
        String parsedText = pdfStripper.getText(pdDoc);

//        PrintWriter pw = new PrintWriter("src/output/pdf.txt");
//        pw.print(parsedText);
//        pw.close();
        return parsedText;
    }
//    public static void convertPdfToHtml(InputStream inputStream) throws IOException, ParserConfigurationException {
//        PDDocument pdf = PDDocument.load(inputStream);
//
//        // Create a transformation matrix to remove margins
//        AffineTransform transform = new AffineTransform();
//        transform.translate(0, 0); // Adjust the values as needed
//
//        PDFRenderer pdfRenderer = new PDFRenderer(pdf);
//
//        // Create a blank image to render the PDF with the transformation
//        int page = 0; // You can adjust the page number
//        BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
//        BufferedImage transformedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
//        transformedImage.createGraphics().drawImage(image, transform, null);
//
//        // Extract text from the transformed image
//        PDFTextStripper textStripper = new PDFTextStripper();
//        String text = textStripper.getText(PDDocument.load(imageToStream(transformedImage)));
//
//        // Write the extracted text to an HTML file
//        Writer output = new PrintWriter("src/output/pdf.html", "utf-8");
//        output.write(text);
//        output.close();
//
//        pdf.close();
//    }
//
//    private static InputStream imageToStream(BufferedImage image) throws IOException {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageIO.write(image, "png", os);
//        return new ByteArrayInputStream(os.toByteArray());
//    }

//    public static void convertPdfToHtml(InputStream inputStream) throws IOException, ParserConfigurationException {
//        PDDocument pdf = PDDocument.load(inputStream);
//        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
//        config.setCssLocator(new CustomCssLocator()); // Custom locator for handling text and images
//        PDFDomTree parser = new PDFDomTree(config);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Writer output = new PrintWriter("output/pdf.html", "utf-8");
//        Document dom = parser.createDOM(pdf);
//        parser.writeTextAndImages(dom, output); // This method processes both text and images
//        output.close();
//        pdf.close();
//    }
//
//    public static class CustomCssLocator extends PDFDomTreeConfig.CssLocator {
//        @Override
//        public String locate(Node node) {
//            if (node instanceof org.fit.pdfdom.Text) {
//                return "text";
//            } else if (node instanceof org.fit.pdfdom.Image) {
//                return "image";
//            }
//            return null;
//        }
//    }
//


}