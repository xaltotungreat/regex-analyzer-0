package org.eclipselabs.real.core.dlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipselabs.real.core.distrib.DistribLeafImpl;
import org.eclipselabs.real.core.distrib.DistribNodeImpl;
import org.eclipselabs.real.core.distrib.DistribNodeRoot;
import org.eclipselabs.real.core.distrib.GenericError;
import org.eclipselabs.real.core.distrib.IDistribLeaf;
import org.eclipselabs.real.core.distrib.IDistribLeafFolder;
import org.eclipselabs.real.core.distrib.IDistribNode;
import org.eclipselabs.real.core.distrib.IDistribRoot;
import org.eclipselabs.real.core.distrib.IDistribTask;
import org.eclipselabs.real.core.logfile.ILogFile;
import org.eclipselabs.real.core.logfile.ILogFileAggregateRep;
import org.eclipselabs.real.core.logfile.LogOperationType;
import org.eclipselabs.real.core.logfile.LogTimeoutPolicy;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.util.LockUtil;
import org.eclipselabs.real.core.util.LockWrapper;

public class DBuilderSearch<R extends ISearchResult<?>> {

    private ILogFileAggregateRep logAggr;
    private ISearchObject<R,?> searchObject;
    private PerformSearchRequest request;

    public DBuilderSearch(ILogFileAggregateRep aggr, ISearchObject<R,?> so, PerformSearchRequest req) {
        logAggr = aggr;
        searchObject = so;
        request = req;
    }

    public IDistribRoot<R, DAccumulatorSearchResult<R>, List<R>, GenericError> buildDistribSystem(
            int threadsNum) {
        DAccumulatorSearchResult<R> accum = new DAccumulatorSearchResult<>();
        IDistribRoot<R, DAccumulatorSearchResult<R>, List<R>, GenericError> root = new DistribNodeRoot<>(accum, threadsNum, "d-" + logAggr.getType().getLogTypeName());
        // add configuration for the root
        List<LockWrapper> lockForOperation = logAggr.getLocksForOperation(LogOperationType.SEARCH);

        root.addLockingParams(
                LockUtil.getLockRunnable(lockForOperation, LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.SEARCH, logAggr)),
                LockUtil.getUnlockRunnable(lockForOperation));

        IDistribNode<R, DAccumulatorSearchResult<R>, List<R>, GenericError> aggrNode = buildNode(root, accum, logAggr);
        // add the only node for this log file aggregate
        root.addNodeChildren(Collections.singletonList(aggrNode));
        return root;
    }

    private IDistribNode<R, DAccumulatorSearchResult<R>, List<R>, GenericError> buildNode(
            IDistribRoot<R, DAccumulatorSearchResult<R>, List<R>, GenericError> root,
            DAccumulatorSearchResult<R> acc,
            ILogFileAggregateRep aggr) {
        IDistribNode<R, DAccumulatorSearchResult<R>, List<R>, GenericError> newNode = new DistribNodeImpl<>(root);
        List<ILogFile> allFiles = aggr.getAllValues();
        List<IDistribLeaf<R, DAccumulatorSearchResult<R>, List<R>, GenericError>> leaves = new ArrayList<>();
        allFiles.stream().forEach(lf -> leaves.add(this.buildLeaf(newNode, acc, lf)));
        newNode.addLeafChildren(leaves);
        return newNode;
    }

    private IDistribLeaf<R, DAccumulatorSearchResult<R>, List<R>, GenericError> buildLeaf(
            IDistribLeafFolder<R, DAccumulatorSearchResult<R>, List<R>, GenericError> pr,
            DAccumulatorSearchResult<R> acc, ILogFile lf) {
        return new DistribLeafImpl<>(pr, buildTask(lf), acc);
    }

    private IDistribTask<R> buildTask(ILogFile lf) {
        return new DTaskLogFileSearch<>(lf, searchObject, request);
    }
}
