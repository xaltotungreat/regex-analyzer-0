package org.eclipselabs.real.core.dlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipselabs.real.core.distrib.DistribFactoryMain;
import org.eclipselabs.real.core.distrib.GenericError;
import org.eclipselabs.real.core.distrib.IDistribLeaf;
import org.eclipselabs.real.core.distrib.IDistribLeafFolder;
import org.eclipselabs.real.core.distrib.IDistribNode;
import org.eclipselabs.real.core.distrib.IDistribRoot;
import org.eclipselabs.real.core.distrib.IDistribTask;
import org.eclipselabs.real.core.distrib.IDistribTaskResultWrapper;
import org.eclipselabs.real.core.logfile.ILogFileAggregateRead;
import org.eclipselabs.real.core.logfile.ILogFileRead;
import org.eclipselabs.real.core.logfile.LogOperationType;
import org.eclipselabs.real.core.logfile.LogTimeoutPolicy;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.util.LockUtil;
import org.eclipselabs.real.core.util.LockWrapper;

public class DBuilderSearch<R extends ISearchResult<?>> {

    private ILogFileAggregateRead logAggr;
    private ISearchObject<R,?> searchObject;
    private PerformSearchRequest request;

    public DBuilderSearch(ILogFileAggregateRead aggr, ISearchObject<R,?> so, PerformSearchRequest req) {
        logAggr = aggr;
        searchObject = so;
        request = req;
    }

    public IDistribRoot<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> build(int threadsNum) {
        DAccumulatorSearchResult<R> accum = new DAccumulatorSearchResult<>();
        IDistribRoot<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> root =
                DistribFactoryMain.INSTANCE.getDefaultFactory().getRoot(accum, threadsNum, "d-" + logAggr.getType().getLogTypeName());
        // add configuration for the root
        List<LockWrapper> lockForOperation = logAggr.getLocksForOperation(LogOperationType.SEARCH);

        root.addLockingParams(
                LockUtil.getLockRunnable(lockForOperation, LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.SEARCH_WAIT, logAggr)),
                LockUtil.getUnlockRunnable(lockForOperation));
        root.setExecutionTimeout(LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.SEARCH, logAggr));

        IDistribNode<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> aggrNode = buildNode(root, accum, logAggr);
        // add the only node for this log file aggregate
        root.addNodeChildren(Collections.singletonList(aggrNode));
        return root;
    }

    private IDistribNode<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> buildNode(
            IDistribRoot<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> root,
            DAccumulatorSearchResult<R> acc,
            ILogFileAggregateRead aggr) {
        IDistribNode<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> newNode =
                DistribFactoryMain.INSTANCE.getDefaultFactory().getNode(root);
        List<ILogFileRead> allFiles = aggr.getAllValues();
        List<IDistribLeaf<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError>> leaves = new ArrayList<>();
        allFiles.stream().forEach(lf -> leaves.add(this.buildLeaf(newNode, acc, lf)));
        newNode.addLeafChildren(leaves);
        return newNode;
    }

    private IDistribLeaf<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> buildLeaf(
            IDistribLeafFolder<R, DAccumulatorSearchResult<R>, List<IDistribTaskResultWrapper<R>>, GenericError> pr,
            DAccumulatorSearchResult<R> acc, ILogFileRead lf) {
        return DistribFactoryMain.INSTANCE.getDefaultFactory().getLeaf(pr, buildTask(lf), acc);
    }

    private IDistribTask<R> buildTask(ILogFileRead lf) {
        /* It is VERY IMPORTANT to have a clone of the search request with the same monitor
         * Some parameters including replace tables are copied and therefore they can be modified
         * by the search object. Only the monitor is shared to update the GUI
         */
        return new DTaskLogFileSearch<>(lf, searchObject, request.getSharedMonitorCopy());
    }
}
