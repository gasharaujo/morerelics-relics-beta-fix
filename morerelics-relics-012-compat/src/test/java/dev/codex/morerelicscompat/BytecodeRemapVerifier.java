package dev.codex.morerelicscompat;

import dev.codex.morerelicscompat.mixin.LegacyReferenceRemapPlugin;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public final class BytecodeRemapVerifier {
    private static final String MARKER =
        "dev.codex.morerelicscompat.mixin.LegacyReferenceRemapMixin";
    private static final String LEGACY_FACADE_PREFIX = "dev/codex/morerelicscompat/legacy/";

    private static final Set<String> REMOVED_TYPES = Set.of(
        "it/hurts/sskirillss/relics/components/AbilityComponent",
        "it/hurts/sskirillss/relics/init/EffectRegistry",
        "it/hurts/sskirillss/relics/items/relics/base/IRelicItem",
        "it/hurts/sskirillss/relics/items/relics/base/data/RelicData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilitiesData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilityData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/StatData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourceData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourcesData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/misc/UpgradeOperation",
        "it/hurts/sskirillss/relics/items/relics/base/data/loot/LootData",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/CastData",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastType",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/PredicateType",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastStage"
    );

    private BytecodeRemapVerifier() {
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 2) {
            throw new IllegalArgumentException("Expected: <MoreRelics jar> <compat jar>");
        }

        Map<String, ClassNode> facadeClasses = readClasses(Path.of(arguments[1]));
        LegacyReferenceRemapPlugin plugin = new LegacyReferenceRemapPlugin();
        int transformed = 0;
        int facadeReferences = 0;
        Set<String> activationHandlers = new HashSet<>();
        String activationDescriptor = facadeClasses.get(LEGACY_FACADE_PREFIX + "LegacyRelicItem")
            .methods.stream().filter(method -> method.name.equals("castActiveAbility"))
            .findFirst().orElseThrow().desc;

        try (JarFile moreRelics = new JarFile(arguments[0])) {
            for (var entries = moreRelics.entries(); entries.hasMoreElements();) {
                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                ClassNode target = readClass(moreRelics.getInputStream(entry));
                if (!referencesRemovedType(target)) {
                    continue;
                }

                plugin.preApply(target.name.replace('/', '.'), target, MARKER, null);
                assertRemovedTypesGone(target);
                facadeReferences += verifyFacadeReferences(target, facadeClasses);
                for (var method : target.methods) {
                    if (method.name.equals("castActiveAbility")) {
                        if (!method.desc.equals(activationDescriptor)) {
                            throw new IllegalStateException("Activation handler does not override the bridge: "
                                + target.name + method.desc);
                        }
                        activationHandlers.add(target.name);
                    }
                }
                transformed++;
            }
        }

        if (transformed != 31) {
            throw new IllegalStateException("Expected 31 transformed classes, got " + transformed);
        }
        for (String item : Set.of("KingCrimson", "WeaversSpool")) {
            if (!activationHandlers.contains("com/blorb/morerelics/relics/" + item)) {
                throw new IllegalStateException("Missing remapped activation handler for " + item);
            }
        }

        System.out.println(
            "Verified " + transformed + " transformed classes and "
                + facadeReferences + " legacy facade member references."
        );
        System.out.println("Verified " + activationHandlers.size()
            + " More Relics activation handler signatures, including KingCrimson and WeaversSpool.");
    }

    private static boolean referencesRemovedType(ClassNode classNode) {
        for (var field : classNode.fields) {
            if (containsRemovedType(field.desc)) {
                return true;
            }
        }
        for (var method : classNode.methods) {
            if (containsRemovedType(method.desc)) {
                return true;
            }
            for (var instruction : method.instructions) {
                String text = switch (instruction) {
                    case MethodInsnNode methodInsn -> methodInsn.owner + methodInsn.desc;
                    case FieldInsnNode field -> field.owner + field.desc;
                    case TypeInsnNode type -> type.desc;
                    case InvokeDynamicInsnNode dynamic -> dynamic.desc
                        + java.util.Arrays.toString(dynamic.bsmArgs);
                    case LdcInsnNode constant -> String.valueOf(constant.cst);
                    case MultiANewArrayInsnNode array -> array.desc;
                    default -> "";
                };
                if (containsRemovedType(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void assertRemovedTypesGone(ClassNode classNode) {
        if (referencesRemovedType(classNode)) {
            throw new IllegalStateException("Legacy type remains after remap in " + classNode.name);
        }
    }

    private static boolean containsRemovedType(String value) {
        if (value == null) {
            return false;
        }
        for (String removedType : REMOVED_TYPES) {
            if (value.contains(removedType)) {
                return true;
            }
        }
        return false;
    }

    private static int verifyFacadeReferences(
        ClassNode target,
        Map<String, ClassNode> facadeClasses
    ) {
        int verified = 0;
        for (var method : target.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call
                    && call.owner.startsWith(LEGACY_FACADE_PREFIX)) {
                    ClassNode owner = requireClass(facadeClasses, call.owner, target.name);
                    boolean exists = owner.methods.stream()
                        .anyMatch(candidate -> candidate.name.equals(call.name)
                            && candidate.desc.equals(call.desc));
                    if (!exists) {
                        throw new IllegalStateException(
                            "Missing facade method " + call.owner + "." + call.name + call.desc
                                + " referenced by " + target.name
                        );
                    }
                    verified++;
                } else if (instruction instanceof FieldInsnNode access
                    && access.owner.startsWith(LEGACY_FACADE_PREFIX)) {
                    ClassNode owner = requireClass(facadeClasses, access.owner, target.name);
                    boolean exists = owner.fields.stream()
                        .anyMatch(candidate -> candidate.name.equals(access.name)
                            && candidate.desc.equals(access.desc));
                    if (!exists) {
                        throw new IllegalStateException(
                            "Missing facade field " + access.owner + "." + access.name + access.desc
                                + " referenced by " + target.name
                        );
                    }
                    verified++;
                }
            }
        }
        return verified;
    }

    private static ClassNode requireClass(
        Map<String, ClassNode> classes,
        String owner,
        String target
    ) {
        ClassNode result = classes.get(owner);
        if (result == null) {
            throw new IllegalStateException(
                "Missing facade class " + owner + " referenced by " + target
            );
        }
        return result;
    }

    private static Map<String, ClassNode> readClasses(Path jarPath) throws IOException {
        Map<String, ClassNode> result = new HashMap<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            for (var entries = jar.entries(); entries.hasMoreElements();) {
                var entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    ClassNode classNode = readClass(jar.getInputStream(entry));
                    result.put(classNode.name, classNode);
                }
            }
        }
        return result;
    }

    private static ClassNode readClass(InputStream input) throws IOException {
        try (input) {
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, 0);
            return node;
        }
    }
}
