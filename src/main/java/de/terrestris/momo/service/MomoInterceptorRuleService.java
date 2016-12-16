package de.terrestris.momo.service;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import de.terrestris.shogun2.dao.InterceptorRuleDao;
import de.terrestris.shogun2.model.interceptor.InterceptorRule;
import de.terrestris.shogun2.service.InterceptorRuleService;
import de.terrestris.shogun2.util.enumeration.HttpEnum;
import de.terrestris.shogun2.util.enumeration.InterceptorEnum.RuleType;
import de.terrestris.shogun2.util.enumeration.OgcEnum;
import de.terrestris.shogun2.util.enumeration.OgcEnum.ServiceType;

@Service("momoInterceptorRuleService")
public class MomoInterceptorRuleService extends InterceptorRuleService<InterceptorRule,
		InterceptorRuleDao<InterceptorRule>> {

	/**
	 * The Logger
	 */
	private static final Logger LOG = Logger.getLogger(MomoInterceptorRuleService.class);

	/**
	 * Creates and saves all the {@link InterceptorRule}s for the passed
	 * endpoint and {@link RuleType}. Effectively this will create a single rule
	 * for all the combinations of Request/Response {@link HttpEnum.EventType},
	 * WMS/WFS/WCS {@link ServiceType} and the associated operations per
	 * {@link ServiceType}.
	 *
	 * @param endpoint
	 * @param rt
	 */
	public void createAllRelevantOgcRules(String endpoint, RuleType rt) {
		for (HttpEnum.EventType et : HttpEnum.EventType.values()) {
			for (ServiceType st : ServiceType.values()) {
				Set<OgcEnum.OperationType> ops = OgcEnum.OPERATIONS_BY_SERVICETYPE.get(st);
				for (OgcEnum.OperationType op : ops) {
					InterceptorRule rule = new InterceptorRule(et, rt, st, op, endpoint);
					LOG.trace("Creating and saving interceptor rule " + rule);
					this.saveOrUpdate(rule);
				}
			}
		}
	}

}