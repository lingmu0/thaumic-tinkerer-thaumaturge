package net.lingmu.thaumictinkerer.focus;

import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import java.util.HashMap;
import net.minecraft.world.item.ItemStack;

/** Keep independently carried blocks distinct in Thaumaturge's spell-hash focus picker. */
final class FocusStorageIdentity {
    static void update(ItemStack focus,boolean carrying,int hash) {
        Spell spell=Spells.spellOf(focus);if(spell==null)return;
        SpellNode root=spell.root();var settings=new HashMap<>(root.settings());
        if(carrying)settings.put("tt_dislocation_hash",hash);else settings.remove("tt_dislocation_hash");
        Spells.setSpell(focus,new Spell(spell.style(),new SpellNode(root.part(),root.aspect(),settings,root.children())));
    }
    private FocusStorageIdentity() {}
}
