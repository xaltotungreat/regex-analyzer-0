package org.eclipselabs.real.core.dlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipselabs.real.core.distrib.DistribFactoryMain;
import org.eclipselabs.real.core.distrib.GenericError;
import org.eclipselabs.real.core.distrib.IDistribLeaf;
import org.eclipselabs.real.core.distrib.IDistribLeafFolder;
import org.eclipselabs.real.core.distrib.IDistribNode;
import org.eclipselabs.real.core.distrib.IDistribRoot;
import org.eclipselabs.real.core.distrib.IDistribTask;
import org.eclipselabs.real.core.logfile.ILogAggregateRepository;
import org.eclipselabs.real.core.logfile.ILogFileAggregate;
import org.eclipselabs.real.core.logfile.LogFileAggregateInfo;
import org.eclipselabs.real.core.logfile.LogFileController;
import org.eclipselabs.real.core.logfile.LogOperationType;
import org.eclipselabs.real.core.logfile.LogTimeoutPolicy;
import org.eclipselabs.real.core.util.LockUtil;
import org.eclipselabs.real.core.util.LockWrapper;

public class DBuilderReloadFolders {

    private Set<String> folders;
    private ILogAggregateRepository aggrRep;

    public DBuilderReloadFolders(Set<String> flds, ILogAggregateRepository rep) {
        folders = flds;
        aggrRep = rep;
    }

    public IDistribRoot<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> build(int threadsNum) {
        DAccumulatorReloadFolders accum = new DAccumulatorReloadFolders(aggrRep.getCount());
        IDistribRoot<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> root =
                DistribFactoryMain.INSTANCE.getDefaultFactory().getRoot(accum, threadsNum, "reload-fld");
        // add configuration for the root
        List<LockWrapper> locksForOperation = LogFileController.INSTANCE.getLocksForOperation(LogOperationType.CONTROLLER_RELOAD_FOLDERS);

        root.addLockingParams(
                LockUtil.getLockRunnable(locksForOperation, LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.CONTROLLER_RELOAD_FOLDERS_WAIT, null)),
                LockUtil.getUnlockRunnable(locksForOperation));
        root.setExecutionTimeout(LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.CONTROLLER_RELOAD_FOLDERS, null));

        IDistribNode<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> aggrNode = buildNode(root, accum);
        // add the only node for this log file aggregate
        root.addNodeChildren(Collections.singletonList(aggrNode));
        return root;
    }

    private IDistribNode<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> buildNode(
            IDistribRoot<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> root,
            DAccumulatorReloadFolders acc) {
        IDistribNode<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> newNode =
                DistribFactoryMain.INSTANCE.getDefaultFactory().getNode(root);
        List<ILogFileAggregate> allFiles = aggrRep.getAllValuesFull();
        List<IDistribLeaf<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError>> leaves = new ArrayList<>();
        allFiles.stream().forEach(lf -> leaves.add(this.buildLeaf(newNode, acc, lf)));
        newNode.addLeafChildren(leaves);
        return newNode;
    }

    private IDistribLeaf<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> buildLeaf(
            IDistribLeafFolder<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> pr,
            DAccumulatorReloadFolders acc,
            ILogFileAggregate aggr) {
        return DistribFactoryMain.INSTANCE.getDefaultFactory().getLeaf(pr, buildTask(folders, aggr), acc);
    }

    private IDistribTask<LogFileAggregateInfo> buildTask(Set<String> flds, ILogFileAggregate aggr) {
        return new DTaskLogFileReloadFolders(flds, aggr);
    }
}
