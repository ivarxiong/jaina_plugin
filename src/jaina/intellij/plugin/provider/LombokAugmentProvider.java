package jaina.intellij.plugin.provider;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ContainerUtil;
import jaina.intellij.plugin.processor.Processor;
import jaina.intellij.plugin.processor.modifier.ModifierProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Provides support for lombok generated elements
 *
 * @author Plushnikov Michail
 */
public class LombokAugmentProvider extends PsiAugmentProvider {
  private static final Logger log = Logger.getInstance(LombokAugmentProvider.class.getName());

  private final Collection<ModifierProcessor> modifierProcessors;

  public LombokAugmentProvider() {
    log.debug("LombokAugmentProvider created");

    modifierProcessors = Arrays.asList(getModifierProcessors());
  }

  /**
   * Provides a simple way to inject modifiers into older versions of IntelliJ. Return of the null value is dictated by legacy IntelliJ API.
   *
   * @param modifierList PsiModifierList that is being queried
   * @param name         String name of the PsiModifier
   * @return {@code Boolean.TRUE} if modifier exists (explicitly set by modifier transformers of the plugin), {@code null} otherwise.
   */
  public Boolean hasModifierProperty(@NotNull PsiModifierList modifierList, @NotNull final String name) {
    if (DumbService.isDumb(modifierList.getProject())) {
      return null;
    }

    final Set<String> modifiers = this.transformModifiers(modifierList, Collections.<String>emptySet());
    if (modifiers.contains(name)) {
      return Boolean.TRUE;
    }

    return null;
  }

  @NotNull
  @Override
  protected Set<String> transformModifiers(@NotNull PsiModifierList modifierList, @NotNull final Set<String> modifiers) {
    // make copy of original modifiers
    Set<String> result = ContainerUtil.newHashSet(modifiers);

    // Loop through all available processors and give all of them a chance to respond
    for (ModifierProcessor processor : modifierProcessors) {
      if (processor.isSupported(modifierList)) {
        processor.transformModifiers(modifierList, result);
      }
    }

    return result;
  }

  /*
  @Nullable
  @Override
  protected PsiType inferType(@NotNull PsiTypeElement typeElement) {
    if (DumbService.isDumb(typeElement.getProject()) || !valProcessor.isEnabled(typeElement.getProject())) {
      return null;
    }
    return valProcessor.inferType(typeElement);
  }*/

  @NotNull
  @Override
  public <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull final Class<Psi> type) {
    final List<Psi> emptyResult = Collections.emptyList();
    if ((type != PsiClass.class && type != PsiField.class && type != PsiMethod.class) || !(element instanceof PsiExtensibleClass)) {
      return emptyResult;
    }
    // Don't filter !isPhysical elements or code auto completion will not work
    if (!element.isValid()) {
      return emptyResult;
    }

    final PsiClass psiClass = (PsiClass) element;
    // Skip processing of Annotations and Interfaces
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      return emptyResult;
    }
    // skip processing if plugin is disabled
    final Project project = element.getProject();

    if (type == PsiField.class) {
      return CachedValuesManager.getCachedValue(element, new FieldLombokCachedValueProvider<Psi>(type, psiClass));
    } else if (type == PsiMethod.class) {
      return CachedValuesManager.getCachedValue(element, new MethodLombokCachedValueProvider<Psi>(type, psiClass));
    } else if (type == PsiClass.class) {
      return CachedValuesManager.getCachedValue(element, new ClassLombokCachedValueProvider<Psi>(type, psiClass));
    } else {
      return emptyResult;
    }
  }

  private ModifierProcessor[] getModifierProcessors() {
    return new ModifierProcessor[0];
  }

  private static class FieldLombokCachedValueProvider<Psi extends PsiElement> extends LombokCachedValueProvider<Psi> {
    FieldLombokCachedValueProvider(Class<Psi> type, PsiClass psiClass) {
      super(type, psiClass);
    }
  }

  private static class MethodLombokCachedValueProvider<Psi extends PsiElement> extends LombokCachedValueProvider<Psi> {
    MethodLombokCachedValueProvider(Class<Psi> type, PsiClass psiClass) {
      super(type, psiClass);
    }
  }

  private static class ClassLombokCachedValueProvider<Psi extends PsiElement> extends LombokCachedValueProvider<Psi> {
    ClassLombokCachedValueProvider(Class<Psi> type, PsiClass psiClass) {
      super(type, psiClass);
    }
  }

  private static class LombokCachedValueProvider<Psi extends PsiElement> implements CachedValueProvider<List<Psi>> {
    private final Class<Psi> type;
    private final PsiClass psiClass;

    LombokCachedValueProvider(Class<Psi> type, PsiClass psiClass) {
      this.type = type;
      this.psiClass = psiClass;
    }

    @Nullable
    @Override
    public Result<List<Psi>> compute() {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Process call for type: %s class: %s", type, psiClass.getQualifiedName()));
      }

      final List<Psi> result = new ArrayList<Psi>();
      final Collection<Processor> lombokProcessors = LombokProcessorProvider.getInstance(psiClass.getProject()).getLombokProcessors(type);
      for (Processor processor : lombokProcessors) {
        result.addAll((Collection<? extends Psi>) processor.process(psiClass));
      }
      return Result.create(result, PsiModificationTracker.JAVA_STRUCTURE_MODIFICATION_COUNT);
    }
  }
}
