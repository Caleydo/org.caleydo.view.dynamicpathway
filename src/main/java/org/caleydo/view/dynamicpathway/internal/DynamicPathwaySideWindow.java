package org.caleydo.view.dynamicpathway.internal;

import gleem.linalg.Vec4f;

import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions.IMoveTransition;

import com.google.common.base.Supplier;

public class DynamicPathwaySideWindow extends DynamicPathwayWindow {
	
	private final IMoveTransition animation;

	public DynamicPathwaySideWindow(String title, DynamicPathwayView view, IMoveTransition animation) {
		super(title, view);
		this.animation = animation;
	}
	
	@Override
	public <T> T getLayoutDataAs(Class<T> clazz, Supplier<? extends T> default_) {
		if (clazz.isInstance(animation)) {
			return clazz.cast(animation);
		}
		return super.getLayoutDataAs(clazz, default_);
	}
	
	public static final IMoveTransition SLIDE_LEFT_OUT = new IMoveTransition() {
		@Override
		public Vec4f move(Vec4f from, Vec4f to, float w, float h, float alpha) {
			boolean isSlideOut = to.z() <= 1;
			boolean isSlideIn = from.z() <= 1;
			if (isSlideOut) {
				// keep the size and just move to the left
				Vec4f r = new Vec4f();
				r.setX(from.x() - (from.z() - to.z()) * alpha); // keep the size and move it out
				r.setY(to.y()); // final y
				r.setZ(from.z()); // original width
				r.setW(to.w()); // final height
				return r;
			} else if (isSlideIn) {
				Vec4f r = new Vec4f();
				r.setX(from.x() + (from.z() - to.z()) * (1 - alpha)); // keep the size and move it out
				r.setY(to.y()); // target y
				r.setZ(to.z()); // target width with is the real with
				r.setW(to.w()); // target height
				return r;
			} else
				return MoveTransitions.MOVE_AND_GROW_LINEAR.move(from, to, w, h, alpha);
		}
	};

}
