package org.caleydo.view.dynamicpathway.ranking;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.IScrollBar;
import org.caleydo.core.view.opengl.layout2.basic.ScrollBarCompatibility;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.util.GLElementWindow;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwaySideWindow;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwayView;
import org.caleydo.view.entourage.EEmbeddingID;
import org.caleydo.view.entourage.GLEntourage;
import org.caleydo.view.entourage.ranking.PathwayRow;
import org.caleydo.vis.lineup.config.IRankTableUIConfig;
import org.caleydo.vis.lineup.config.RankTableConfigBase;
import org.caleydo.vis.lineup.config.RankTableUIConfigBase;
import org.caleydo.vis.lineup.config.IRankTableUIConfig.EButtonBarPositionMode;
import org.caleydo.vis.lineup.model.CategoricalRankColumnModel;
import org.caleydo.vis.lineup.model.IRow;
import org.caleydo.vis.lineup.model.RankTableModel;
import org.caleydo.vis.lineup.model.StringRankColumnModel;
import org.caleydo.vis.lineup.model.mixin.ICollapseableColumnMixin;
import org.caleydo.vis.lineup.ui.TableUI;

import com.google.common.base.Function;

public class RankingElement extends GLElementContainer {
	
	private final static int PATHWAY_NAME_COLUMN_WIDTH = 140;
	private final static int PATHWAY_DATABASE_COLUMN_WIDTH = 70;
	private final static int RANK_COLUMN_WIDTH = 50;
	
	private GLElementWindow window;
	private final DynamicPathwayView view;
	private final RankTableModel table;
	private StringRankColumnModel pathwayNameColumn;
	private CategoricalRankColumnModel<?> pathwayDataBaseColumn;
	private TableUI tableUI;

	public RankingElement(final DynamicPathwayView view) {
		this.view = view;
		
		this.table = new RankTableModel(new RankTableConfigBase());
//		table.addPropertyChangeListener(RankTableModel.PROP_SELECTED_ROW, onSelectRow);
//		table.addPropertyChangeListener(RankTableModel.PROP_COLUMNS, onModifyColumn);
		
		initTable(table);
		
		setLayout(GLLayouts.flowVertical(0));
//		IRankTableUIConfig config = new RankTableUIConfigBase(true, false, false) {
//			@Override
//			public IScrollBar createScrollBar(boolean horizontal) {
//				return new ScrollBarCompatibility(horizontal, view.getDndController());
//			}
//
//			@Override
//			public void renderIsOrderByGlyph(GLGraphics g, float w, float h, boolean orderByIt) {
//				// no highlight
//			}
//
//			@Override
//			public EButtonBarPositionMode getButtonBarPosition() {
//				return EButtonBarPositionMode.OVER_LABEL;
//			}
//
//			@Override
//			public void renderRowBackground(GLGraphics g, Rect rect, boolean even, IRow row,
//					IRow selected) {
////				renderRowBackgroundImpl(g, rect.x(), rect.y(), rect.width(), rect.height(), even, row, selected);
//			}
//
//			@Override
//			public boolean canEditValues() {
//				return false;
//			}
//
//			@Override
//			public boolean isSmallHeaderByDefault() {
//				return false;
//			}
//		};

		
		
	}
	
	private final PropertyChangeListener onSelectRow = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			rowSelected((PathwayRow) evt.getNewValue());
		}
	};

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (w < 10)
			return;
		super.renderImpl(g, w, h);
//		g.drawText("hallo", 0, 0, w, 20);
	}
	
	public void setWindow(GLElementWindow window) {
		this.window = window;
	}
	
	private void rowSelected(PathwayRow newValue) {

		if (newValue == null)
			return;

//		if (!view.isPathwayInPathwayInfos(newValue.getPathway()))
//			view.addPathway(newValue.getPathway());

		table.setSelectedRow(null);
	}
	
	private void initTable(RankTableModel table) {
		pathwayNameColumn = new StringRankColumnModel(GLRenderers.drawText("Pathway", VAlign.CENTER),
				StringRankColumnModel.DEFAULT, Color.GRAY, new Color(.95f, .95f, .95f),
				StringRankColumnModel.FilterStrategy.SUBSTRING);
		pathwayNameColumn.setWidth(PATHWAY_NAME_COLUMN_WIDTH);
		pathwayNameColumn.addPropertyChangeListener(ICollapseableColumnMixin.PROP_COLLAPSED, onCollapseColumn);
		table.add(pathwayNameColumn);
		
		//TODO: just KEGG
		Collection<String> dbtypes = new ArrayList<>(2);
		for (EPathwayDatabaseType type : EPathwayDatabaseType.values()) {
			dbtypes.add(type.getName());
		}
		pathwayDataBaseColumn = CategoricalRankColumnModel.createSimple(
				GLRenderers.drawText("Pathway Type", VAlign.CENTER), new Function<IRow, String>() {
					@Override
					public String apply(IRow in) {
						PathwayRow r = (PathwayRow) in;
						return r.getPathway().getType().getName();
					}
				}, dbtypes);
		
		pathwayDataBaseColumn.setWidth(PATHWAY_DATABASE_COLUMN_WIDTH);
		pathwayDataBaseColumn.setCollapsed(true);
		pathwayDataBaseColumn.addPropertyChangeListener(ICollapseableColumnMixin.PROP_COLLAPSED, onCollapseColumn);
		table.add(pathwayDataBaseColumn);
		
		List<PathwayRow> data = new ArrayList<>();
		for (PathwayGraph g : PathwayManager.get().getAllItems()) {
			data.add(new PathwayRow(g));
		}
		Collections.sort(data);

		table.addData(data);
		
	}
	
	private final PropertyChangeListener onCollapseColumn = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			((AnimatedGLElementContainer) window.getParent()).resizeChild(window, 200, Float.NaN);
			// window.setSize(getRequiredWidth(), Float.NaN);
		}
	};

	

}
