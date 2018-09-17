package jaina.intellij.plugin.processor.field;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import jaina.annotation.Layout;
import jaina.intellij.plugin.model.ProblemBuilder;
import jaina.intellij.plugin.processor.AbstractClassProcessor;
import jaina.intellij.plugin.psi.LombokLightFieldBuilder;
import jaina.intellij.plugin.util.PsiAnnotationUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kuma on 2018/9/14.
 */
public class ViewFieldProcessor extends AbstractClassProcessor {

    public ViewFieldProcessor() {
        super(PsiField.class, Layout.class);
    }

    @Override
    protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
        return true;
    }

    @Override
    protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
        PsiAnnotationMemberValue psiAnnotationMemberValue = psiAnnotation.findAttributeValue("value");
        if(psiAnnotationMemberValue != null) {
            String fileName = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "value");
            PsiFile[] psiFile = getPisFile(psiClass, fileName);
            if(psiFile.length > 0) {
                List<ViewElement> elements = new ArrayList<>();
                getViewsFromLayout(psiFile[0], elements);
                for(ViewElement element: elements) {
                    target.add(createLoggerField(psiClass, element, psiAnnotation));
                }
            }
        }
    }

    private LombokLightFieldBuilder createLoggerField(@NotNull PsiClass psiClass, ViewElement element, @NotNull PsiAnnotation psiAnnotation) {
        final PsiManager manager = psiClass.getContainingFile().getManager();
        final Project project = psiClass.getProject();
        final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
        final PsiType psiViewType = psiElementFactory.createTypeFromText(element.getFieldType(), psiClass);
        LombokLightFieldBuilder viewField = new LombokLightFieldBuilder(manager, element.getFieldName(), psiViewType)
                .withContainingClass(psiClass)
                .withModifier(PsiModifier.PROTECTED)
                .withNavigationElement(psiAnnotation);
        return viewField;
    }

    private PsiFile[] getPisFile(PsiClass psiClass, String fileName) {
        String name = fileName + ".xml";
        PsiFile[] psiFile = FilenameIndex.getFilesByName(psiClass.getProject(), name, GlobalSearchScope.allScope(psiClass.getProject()));
        return  psiFile;
    }

    public static List<ViewElement> getViewsFromLayout(final PsiFile file, final List<ViewElement> elements) {
        file.accept(new XmlRecursiveElementVisitor() {
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;
                    String tagType = tag.getName();
                    if (tagType.equalsIgnoreCase("include")) {
                        XmlAttribute layout = tag.getAttribute("layout", null);
                        Project project = file.getProject();
                        XmlFile include = null;
                        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, getLayoutName(layout.getValue()) + ".xml", GlobalSearchScope.allScope(project));
                        if (psiFiles.length > 0) {
                            include = (XmlFile) psiFiles[0];
                        }
                        if (include != null) {
                            getViewsFromLayout(include, elements);
                            return;
                        }
                    }
                    XmlAttribute id = tag.getAttribute("android:id", null);
                    if (id == null) {
                        return;
                    }
                    String idValue = id.getValue();
                    if (idValue == null) {
                        return;
                    }
                    XmlAttribute aClass = tag.getAttribute("class", null);
                    if (aClass != null) {
                        tagType = aClass.getValue();
                    }
                    try {
                        ViewElement e = new ViewElement(idValue, tagType);
                        elements.add(e);
                    } catch (IllegalArgumentException e) {

                    }
                }
            }
        });
        return elements;
    }

    public static String getLayoutName(String layout) {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null;
        }
        String[] parts = layout.split("/");
        if (parts.length != 2) {
            return null;
        }
        return parts[1];
    }

}
