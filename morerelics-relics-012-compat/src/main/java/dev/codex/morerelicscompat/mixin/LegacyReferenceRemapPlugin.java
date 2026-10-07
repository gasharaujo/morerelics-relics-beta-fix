package dev.codex.morerelicscompat.mixin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Rewrites only the Relics API classes removed in 0.12 to local facade types. */
public final class LegacyReferenceRemapPlugin implements IMixinConfigPlugin {
    private static final String MARKER_MIXIN =
        "dev.codex.morerelicscompat.mixin.LegacyReferenceRemapMixin";

    private static final Map<String, String> TYPE_MAP = Map.ofEntries(
        Map.entry(
            "it/hurts/sskirillss/relics/components/AbilityComponent",
            "dev/codex/morerelicscompat/legacy/LegacyAbilityComponent"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/init/EffectRegistry",
            "dev/codex/morerelicscompat/legacy/LegacyEffectRegistry"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/IRelicItem",
            "dev/codex/morerelicscompat/legacy/LegacyRelicItem"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/RelicData",
            "dev/codex/morerelicscompat/legacy/data/RelicData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilitiesData",
            "dev/codex/morerelicscompat/legacy/data/leveling/AbilitiesData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilityData",
            "dev/codex/morerelicscompat/legacy/data/leveling/AbilityData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/leveling/StatData",
            "dev/codex/morerelicscompat/legacy/data/leveling/StatData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingData",
            "dev/codex/morerelicscompat/legacy/data/leveling/LevelingData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourceData",
            "dev/codex/morerelicscompat/legacy/data/leveling/LevelingSourceData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourcesData",
            "dev/codex/morerelicscompat/legacy/data/leveling/LevelingSourcesData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/leveling/misc/UpgradeOperation",
            "dev/codex/morerelicscompat/legacy/data/leveling/misc/UpgradeOperation"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/loot/LootData",
            "dev/codex/morerelicscompat/legacy/data/loot/LootData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/cast/CastData",
            "dev/codex/morerelicscompat/legacy/data/cast/CastData"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastType",
            "dev/codex/morerelicscompat/legacy/data/cast/misc/CastType"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/PredicateType",
            "dev/codex/morerelicscompat/legacy/data/cast/misc/PredicateType"
        ),
        Map.entry(
            "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastStage",
            "dev/codex/morerelicscompat/legacy/data/cast/misc/CastStage"
        )
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {
        if (MARKER_MIXIN.equals(mixinClassName)) {
            remapClass(targetClass);
        }
    }

    @Override
    public void postApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {
    }

    private static void remapClass(ClassNode classNode) {
        classNode.signature = mapSignature(classNode.signature);

        for (var field : classNode.fields) {
            field.desc = mapDescriptor(field.desc);
            field.signature = mapSignature(field.signature);
            field.value = mapConstant(field.value);
        }

        for (var method : classNode.methods) {
            method.desc = mapDescriptor(method.desc);
            method.signature = mapSignature(method.signature);

            if (method.exceptions != null) {
                method.exceptions.replaceAll(LegacyReferenceRemapPlugin::mapInternalName);
            }

            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof TypeInsnNode typeInsn) {
                    typeInsn.desc = typeInsn.desc.startsWith("[")
                        ? mapDescriptor(typeInsn.desc)
                        : mapInternalName(typeInsn.desc);
                } else if (instruction instanceof FieldInsnNode fieldInsn) {
                    fieldInsn.owner = mapInternalName(fieldInsn.owner);
                    fieldInsn.desc = mapDescriptor(fieldInsn.desc);
                } else if (instruction instanceof MethodInsnNode methodInsn) {
                    methodInsn.owner = mapInternalName(methodInsn.owner);
                    methodInsn.desc = mapDescriptor(methodInsn.desc);
                } else if (instruction instanceof InvokeDynamicInsnNode dynamicInsn) {
                    dynamicInsn.desc = mapDescriptor(dynamicInsn.desc);
                    dynamicInsn.bsm = mapHandle(dynamicInsn.bsm);
                    for (int i = 0; i < dynamicInsn.bsmArgs.length; i++) {
                        dynamicInsn.bsmArgs[i] = mapConstant(dynamicInsn.bsmArgs[i]);
                    }
                } else if (instruction instanceof LdcInsnNode ldcInsn) {
                    ldcInsn.cst = mapConstant(ldcInsn.cst);
                } else if (instruction instanceof MultiANewArrayInsnNode arrayInsn) {
                    arrayInsn.desc = mapDescriptor(arrayInsn.desc);
                } else if (instruction instanceof FrameNode frameInsn) {
                    remapFrameEntries(frameInsn.local);
                    remapFrameEntries(frameInsn.stack);
                }
            }

            if (method.tryCatchBlocks != null) {
                for (var tryCatch : method.tryCatchBlocks) {
                    tryCatch.type = mapInternalName(tryCatch.type);
                }
            }

            if (method.localVariables != null) {
                for (var local : method.localVariables) {
                    local.desc = mapDescriptor(local.desc);
                    local.signature = mapSignature(local.signature);
                }
            }
        }
    }

    private static void remapFrameEntries(List<Object> entries) {
        if (entries == null) {
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            Object entry = entries.get(i);
            if (entry instanceof String internalName) {
                entries.set(i, mapInternalName(internalName));
            }
        }
    }

    private static Object mapConstant(Object value) {
        if (value instanceof Type type) {
            return mapType(type);
        }
        if (value instanceof Handle handle) {
            return mapHandle(handle);
        }
        if (value instanceof ConstantDynamic dynamic) {
            Object[] arguments = new Object[dynamic.getBootstrapMethodArgumentCount()];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = mapConstant(dynamic.getBootstrapMethodArgument(i));
            }
            return new ConstantDynamic(
                dynamic.getName(),
                mapDescriptor(dynamic.getDescriptor()),
                mapHandle(dynamic.getBootstrapMethod()),
                arguments
            );
        }
        return value;
    }

    private static Handle mapHandle(Handle handle) {
        return new Handle(
            handle.getTag(),
            mapInternalName(handle.getOwner()),
            handle.getName(),
            mapDescriptor(handle.getDesc()),
            handle.isInterface()
        );
    }

    private static String mapDescriptor(String descriptor) {
        return descriptor == null ? null : mapType(Type.getType(descriptor)).getDescriptor();
    }

    private static Type mapType(Type type) {
        return switch (type.getSort()) {
            case Type.OBJECT -> Type.getObjectType(mapInternalName(type.getInternalName()));
            case Type.ARRAY -> Type.getType("[".repeat(type.getDimensions())
                + mapType(type.getElementType()).getDescriptor());
            case Type.METHOD -> Type.getMethodType(
                mapType(type.getReturnType()),
                java.util.Arrays.stream(type.getArgumentTypes())
                    .map(LegacyReferenceRemapPlugin::mapType)
                    .toArray(Type[]::new)
            );
            default -> type;
        };
    }

    private static String mapSignature(String signature) {
        if (signature == null) {
            return null;
        }

        String mapped = signature;
        for (var entry : TYPE_MAP.entrySet()) {
            mapped = mapped.replace(entry.getKey(), entry.getValue());
        }
        return mapped;
    }

    private static String mapInternalName(String internalName) {
        if (internalName == null) {
            return null;
        }

        for (var entry : TYPE_MAP.entrySet()) {
            String legacy = entry.getKey();
            if (legacy.equals(internalName)) {
                return entry.getValue();
            }
            if (internalName.startsWith(legacy + "$")) {
                return entry.getValue() + internalName.substring(legacy.length());
            }
        }
        return internalName;
    }
}

