package com.myapp.duckbehavior;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 根据角色给出“本次叫声 + 本次行为”
 */
public class DuckBehaviorService {

    /** 返回给前端的结果对象 */
    public static class DuckBehavior {
        private final DuckRole role;
        private final DuckSound sound;
        private final DuckAction action;

        public DuckBehavior(DuckRole role, DuckSound sound, DuckAction action) {
            this.role = role;
            this.sound = sound;
            this.action = action;
        }

        public DuckRole getRole() { return role; }
        public DuckSound getSound() { return sound; }
        public DuckAction getAction() { return action; }

        public String getSoundText() { return sound.getText(); }
        public String getSoundWavPath() { return sound.getWavPath(); }
        public String getActionText() { return action.getText(); }
    }

    // 可按需要调整：不同角色的候选集合
    private static final List<DuckSound> DONALD_SOUNDS = List.of(
            DuckSound.DONALD_QUACK_1,
            DuckSound.DONALD_QUACK_2
    );

    private static final List<DuckSound> DUCKLING_SOUNDS = List.of(
            DuckSound.DUCKLING_QUACK_1,
            DuckSound.DUCKLING_QUACK_2
    );

    // 行为：先优先给出你已经能做动画的两种（SHAKE/HOP）
    private static final List<DuckAction> DONALD_ACTIONS = List.of(
            DuckAction.SHAKE,   // 你可以实现 animateShake
            DuckAction.SPIN     // 先文字描述也OK
    );

    private static final List<DuckAction> DUCKLING_ACTIONS = List.of(
            DuckAction.HOP,     // 你可以实现 animateHop
            DuckAction.WAVE     // 先文字描述也OK
    );

    /** 给前端的唯一入口：根据角色获取本次行为与声音 */
    public DuckBehavior getBehavior(DuckRole role) {
        if (role == null) throw new IllegalArgumentException("role cannot be null");

        DuckSound sound;
        DuckAction action;

        if (role == DuckRole.DONALD) {
            sound = randomPick(DONALD_SOUNDS);
            action = randomPick(DONALD_ACTIONS);
        } else {
            sound = randomPick(DUCKLING_SOUNDS);
            action = randomPick(DUCKLING_ACTIONS);
        }

        return new DuckBehavior(role, sound, action);
    }

    private static <T> T randomPick(List<T> list) {
        int idx = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(idx);
    }
}

