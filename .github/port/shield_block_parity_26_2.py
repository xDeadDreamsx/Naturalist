from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist")


def main() -> None:
    changed = []
    rhino = ROOT / "server/entity/mob/Rhino.java"
    text = rhino.read_text(encoding="utf-8")
    original = text

    # The old 1.21.1 attacker callback was blockedByShield(LivingEntity). In the concrete 26.2
    # compile surface used by this port there is no safely overridable equivalent. Preserve the
    # behavior through LivingEntityMixin, which observes the defender's successful block path and
    # then calls this explicit Rhino callback.
    text = text.replace(
        "    @Override\n    protected void blockedByItem(LivingEntity defender) {",
        "    public void naturalist$onAttackBlocked(LivingEntity defender) {",
    )
    text = text.replace(
        "    @Override\n    protected void blockedByShield(LivingEntity defender) {",
        "    public void naturalist$onAttackBlocked(LivingEntity defender) {",
    )
    text = text.replace(
        "    protected void blockedByShield(LivingEntity defender) {",
        "    public void naturalist$onAttackBlocked(LivingEntity defender) {",
    )

    if text != original:
        rhino.write_text(text, encoding="utf-8")
        changed.append(str(rhino))

    print(f"26.2 shield-block parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
