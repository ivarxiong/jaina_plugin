package jaina.intellij.plugin.extension;

import com.intellij.openapi.extensions.ExtensionPointName;
import jaina.intellij.plugin.processor.Processor;

/**
 * Date: 21.07.13 Time: 12:54
 */
public class LombokProcessorExtensionPoint {
  public static final ExtensionPointName<Processor> EP_NAME_PROCESSOR = ExtensionPointName.create("Jaina Plugin.processor");
  //public static final ExtensionPointName<ModifierProcessor> EP_NAME_MODIFIER_PROCESSOR = ExtensionPointName.create("Lombook Plugin.modifierProcessor");
}
