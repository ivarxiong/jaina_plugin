package jaina.intellij.plugin.processor;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import jaina.intellij.plugin.model.LombokProblem;
import jaina.intellij.plugin.model.LombokPsiElementUsage;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Created by kuma on 2018/9/7.
 */
public interface Processor {

    @NotNull
    Class<? extends Annotation>[] getSupportedAnnotationClasses();

    @NotNull
    Class<? extends PsiElement> getSupportedClass();

    @NotNull
    Collection<LombokProblem> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation);

    boolean isEnabled(@NotNull PropertiesComponent propertiesComponent);

    @NotNull
    List<? super PsiElement> process(@NotNull PsiClass psiClass);

    LombokPsiElementUsage checkFieldUsage(@NotNull PsiField psiField, @NotNull PsiAnnotation psiAnnotation);

}
